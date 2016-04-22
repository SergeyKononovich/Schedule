package com.grsu.konon.schedule;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang.time.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private class CustomFragmentStudentPagerAdapter extends FragmentStatePagerAdapter {
        List<StudentDay> studentDays;

        public CustomFragmentStudentPagerAdapter(FragmentManager fm, List<StudentDay> studentDays) {
            super(fm);
            this.studentDays = studentDays;
        }

        @Override
        public Fragment getItem(int position) {
            return PageFragment.newInstance(studentDays.get(position));
        }

        @Override
        public int getCount() {
            return studentDays.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd(E).MM(MMM).yyyy");
            String title = dateFormat.format(studentDays.get(position).date);
            if (getCurrentDayPos() == position)
                title += " - сегодня";
            return title;
        }

        public int getCurrentDayPos() {
            Date currentDate = new Date();
            int pos = 0;
            for (StudentDay day : studentDays)
                if (DateUtils.isSameDay(day.date, currentDate))
                    return pos;
                else ++pos;

            return 0;
        }
    }

    public final static int REQUEST_CODE_SETTINGS = 2404;
    public final static String SAVED_IS_FOR_STUDENT = "saved_is_for_student";
    public final static String SAVED_SCHEDULE = "saved_schedule";
    private RelativeLayout layout;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                return true;

            case R.id.menu_settings:
                StartSettingsActivity();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = (RelativeLayout) findViewById(R.id.rl_main);

        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        String jsSchedule = sp.getString(SAVED_SCHEDULE, null);
        if (jsSchedule != null)
            SetScheduleForm(jsSchedule, sp.getBoolean(SAVED_IS_FOR_STUDENT, true));
        else
            SetStartForm();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SETTINGS:
                    if (data.hasExtra(SAVED_SCHEDULE) && data.hasExtra(SAVED_IS_FOR_STUDENT)) {
                        boolean isForStudent = data.getExtras().getBoolean(SAVED_IS_FOR_STUDENT);
                        SetScheduleForm(data.getExtras().getString(SAVED_SCHEDULE), isForStudent);
                    }
                    break;
            }
        }
    }


    private void StartSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 2404);
    }

    private void SetStartForm() {
        layout.removeAllViews();
        LayoutInflater ltInflater = getLayoutInflater();
        ltInflater.inflate(R.layout.form_start, layout, true);
    }

    private void SetScheduleForm(String jsSchedule, boolean isForStudent) {
        SharedPreferences.Editor ed = getPreferences(MODE_PRIVATE).edit();

        try {
            if (isForStudent)
                SetStudentScheduleForm(jsSchedule);
            else
                return;

            ed.putBoolean(SAVED_IS_FOR_STUDENT, isForStudent);
            ed.putString(SAVED_SCHEDULE, jsSchedule).commit();
        }
        catch (Exception ex) {
            ed.putString(SAVED_SCHEDULE, null).commit();
            Toast.makeText(this, getString(R.string.toast_invalid_schedule), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void SetStudentScheduleForm(String jsSchedule) throws ParseException {
        List<StudentDay> studentDays = ParseStudentSchedule(jsSchedule);

        LayoutInflater ltInflater = getLayoutInflater();
        View scheduleView = ltInflater.inflate(R.layout.form_schedule, layout, false);

        final ViewPager pager = (ViewPager) scheduleView.findViewById(R.id.pager);
        final CustomFragmentStudentPagerAdapter pagerAdapter =
                new CustomFragmentStudentPagerAdapter(getSupportFragmentManager(), studentDays);
        pager.setAdapter(pagerAdapter);
        final int currentPos = pagerAdapter.getCurrentDayPos();
        pager.setCurrentItem(currentPos);
        final PagerTabStrip pts = (PagerTabStrip) scheduleView.findViewById(R.id.pager_tab_strip);
        pts.setDrawFullUnderline(true);
        pts.setTabIndicatorColor(Color.GRAY);


        layout.removeAllViews();
        layout.addView(scheduleView);
    }


    private List<StudentDay> ParseStudentSchedule(String jsSchedule) throws ParseException {
        JsonParser parser = new JsonParser();
        JsonObject mainObject = parser.parse(jsSchedule).getAsJsonObject();
        JsonArray days = mainObject.getAsJsonArray("days");
        List<StudentDay> studentDays = new ArrayList<>();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (JsonElement day : days) {
            StudentDay studentDay = new StudentDay();
            JsonObject dayObject = day.getAsJsonObject();
            studentDay.date = format.parse(dayObject.get("date").getAsString());

            JsonArray lessons = dayObject.getAsJsonArray("lessons");
            for (JsonElement lesson : lessons) {
                StudentLesson studentLesson = new StudentLesson();
                JsonObject lessonObject = lesson.getAsJsonObject();
                studentLesson.timeStart = lessonObject.get("timeStart").getAsString();
                studentLesson.timeEnd = lessonObject.get("timeEnd").getAsString();
                studentLesson.type = lessonObject.get("type").getAsString();
                studentLesson.title = lessonObject.get("title").getAsString();
                studentLesson.address = lessonObject.get("address").getAsString();
                studentLesson.room = lessonObject.get("room").getAsString();
                JsonObject subgroupObject = lessonObject.getAsJsonObject("subgroup");
                studentLesson.subgroup = subgroupObject.get("title").getAsString();
                JsonObject teacherObject = lessonObject.getAsJsonObject("teacher");
                studentLesson.teacherFullName = teacherObject.get("fullname").getAsString();
                studentDay.lessons.add(studentLesson);
            }
            studentDays.add(studentDay);
        }

        int maxDays = getResources().getInteger(R.integer.schedule_max_days);
        Date last = studentDays.get(0).date;
        last = org.apache.commons.lang.time.DateUtils.addDays(last, 1);
        for (int i = 1; i < Math.min(studentDays.size(), maxDays); ++i) {
            if (last.before(studentDays.get(i).date)) {
                StudentDay emptyDay = new StudentDay();
                emptyDay.date = last;
                studentDays.add(i, emptyDay);
            }

            last = org.apache.commons.lang.time.DateUtils.addDays(last, 1);
        }

        boolean haveCurrentDay = false;
        Date currentDay = new Date();
        for (StudentDay day : studentDays)
        {
            if (org.apache.commons.lang.time.DateUtils.isSameDay(day.date, currentDay))
            {
                haveCurrentDay = true;
                break;
            }
        }

        if (haveCurrentDay == false) {
            StudentDay emptyDay = new StudentDay();
            emptyDay.date = currentDay;
            studentDays.add(0, new StudentDay());
        }

        last = studentDays.get(0).date;
        last = org.apache.commons.lang.time.DateUtils.addDays(last, 1);
        for (int i = 1; i < Math.min(studentDays.size(), maxDays); ++i) {
            if (last.before(studentDays.get(i).date)) {
                StudentDay emptyDay = new StudentDay();
                emptyDay.date = last;
                studentDays.add(i, emptyDay);
            }

            last = org.apache.commons.lang.time.DateUtils.addDays(last, 1);
        }

        return studentDays;
    }
}
