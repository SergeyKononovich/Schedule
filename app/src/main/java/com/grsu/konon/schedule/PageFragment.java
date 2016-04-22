package com.grsu.konon.schedule;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.grsu.konon.schedule.Helpers.SpecialAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PageFragment extends Fragment {
    private static final String ARGUMENT_STUDENT_DAY = "arg_student_day";
    private static final String ATTRIBUTE_NAME_TIME = "time";
    private static final String ATTRIBUTE_NAME_TITLE = "title";
    private static final String ATTRIBUTE_NAME_SUBGROUP = "subgroup";
    private static final String ATTRIBUTE_NAME_ROOM= "room";
    private static final String ATTRIBUTE_NAME_TEACHER_FULL_NAME = "teacher_full_name";
    private StudentDay studentDay;
    private Context context;
    int index = 0;

    public static PageFragment newInstance(StudentDay studentDay) {
        PageFragment pageFragment = new PageFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable(ARGUMENT_STUDENT_DAY, studentDay);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.studentDay = getArguments().getParcelable(ARGUMENT_STUDENT_DAY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ArrayList<Map<String, Object>> data = new ArrayList<>();;
        Map<String, Object> m ;
        for (StudentLesson lesson : studentDay.lessons) {
            m = new HashMap<>();
            m.put(ATTRIBUTE_NAME_TIME, lesson.timeStart + " - " + lesson.timeEnd);
            m.put(ATTRIBUTE_NAME_ROOM, lesson.room + " (" + lesson.address + ")");
            m.put(ATTRIBUTE_NAME_TITLE, lesson.title + " (" + lesson.type + ") ");
            m.put(ATTRIBUTE_NAME_SUBGROUP, lesson.subgroup);
            m.put(ATTRIBUTE_NAME_TEACHER_FULL_NAME, lesson.teacherFullName);
            data.add(m);
        }

        String[] from = { ATTRIBUTE_NAME_TIME, ATTRIBUTE_NAME_ROOM, ATTRIBUTE_NAME_TITLE,
                ATTRIBUTE_NAME_SUBGROUP, ATTRIBUTE_NAME_TEACHER_FULL_NAME };
        int[] to = { R.id.tv_time, R.id.tv_room, R.id.tv_title,
                R.id.tv_subgroup, R.id.tv_teacher_full_name };

        SpecialAdapter sAdapter =
                new SpecialAdapter(getActivity(), data, R.layout.fragment_student_item, from, to);
        View view = inflater.inflate(R.layout.fragment_student, null);

        SimpleAdapter.ViewBinder binder = new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object object, String value) {
                if (view.equals((TextView) view.findViewById(R.id.tv_subgroup)))
                {
                    TextView v = (TextView) view.findViewById(R.id.tv_subgroup);
                    if (value.isEmpty())
                        v.setVisibility(View.GONE);
                }

                return false;
            }
        };
        sAdapter.setViewBinder(binder);

        ListView lv = (ListView) view.findViewById(R.id.lv);
        lv.setAdapter(sAdapter);

        return view;
    }
}