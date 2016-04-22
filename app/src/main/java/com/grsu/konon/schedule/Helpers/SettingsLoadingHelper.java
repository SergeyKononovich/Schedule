package com.grsu.konon.schedule.Helpers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grsu.konon.schedule.MainActivity;
import com.grsu.konon.schedule.R;
import com.grsu.konon.schedule.SettingsActivity;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static android.content.Context.*;

public class SettingsLoadingHelper
{
    public final static String SAVED_FACULTY = "saved_faculty";
    public final static String SAVED_DEPARTMENT = "saved_department";
    public final static String SAVED_COURSE = "saved_course";
    public final static String SAVED_GROUP = "saved_group";
    public final static String ATTRIBUTE_NAME_VALUE = "spinner_item_value";
    public final static String ATTRIBUTE_NAME_ID = "spinner_item_id";


    public static void asyncStudentSettingsLoad(SettingsActivity sActivity, ProgressDialog pDialog) {
        new AsyncTask<Object, Void, SettingsContainer>() {
            private WeakReference<SettingsActivity> wrActivity;
            private ProgressDialog progressDialog;

            @Override
            protected SettingsContainer doInBackground(Object... params) {
                if (params[0] == null || params[1] == null)
                    throw new NullPointerException();
                SettingsActivity sActivity = (SettingsActivity)params[0];
                wrActivity = new WeakReference(sActivity);
                progressDialog = (ProgressDialog)params[1];
                SettingsContainer settingsCon = new SettingsContainer();

                try {
                    SharedPreferences sPref = sActivity.getPreferences(MODE_PRIVATE);
                    int timeoutSec = sActivity.getResources()
                            .getInteger(R.integer.server_response_timeout_sec);
                    JsonParser parser = new JsonParser();

                    // Get faculties
                    String connectionURL = sActivity.getString(R.string.api_faculties);
                    String jsString = HttpConnectionHelper.doConnectionAction(connectionURL, timeoutSec);
                    JsonObject mainObject = parser.parse(jsString).getAsJsonObject();
                    JsonArray items = mainObject.getAsJsonArray("items");
                    for (JsonElement it : items) {
                        JsonObject itObject = it.getAsJsonObject();
                        int id = itObject.get("id").getAsInt();
                        String value = itObject.get("title").getAsString().toLowerCase();
                        settingsCon.faculties.put(id, value);
                    }
                    // Get default value for faculty
                    String saved = sPref.getString(SAVED_FACULTY, null);
                    Integer curId = tryGetIdByName(saved, settingsCon.faculties);
                    Integer defId = curId == -1 ?
                            settingsCon.faculties.keySet().iterator().next().intValue() : curId;
                    settingsCon.defaultIds.put(SAVED_FACULTY, defId);


                    // Get departments
                    connectionURL = sActivity.getString(R.string.api_departments);
                    jsString = HttpConnectionHelper.doConnectionAction(connectionURL, timeoutSec);
                    mainObject = parser.parse(jsString).getAsJsonObject();
                    items = mainObject.getAsJsonArray("items");
                    for (JsonElement it : items) {
                        JsonObject itObject = it.getAsJsonObject();
                        int id = itObject.get("id").getAsInt();
                        String value = itObject.get("title").getAsString().toLowerCase();
                        settingsCon.departments.put(id, value);
                    }
                    // Get default value for department
                    saved = sPref.getString(SAVED_DEPARTMENT, null);
                    curId = tryGetIdByName(saved, settingsCon.departments);
                    defId = curId == -1 ?
                            settingsCon.departments.keySet().iterator().next().intValue() : curId;
                    settingsCon.defaultIds.put(SAVED_DEPARTMENT, defId);


                    // Get default value for course
                    curId = sPref.getInt(SAVED_COURSE, -1);
                    defId = curId == -1 ? 1 : curId;
                    settingsCon.defaultIds.put(SAVED_COURSE, defId);


                    // Get groups
                    connectionURL = sActivity.getString(R.string.api_groups)
                            + "?facultyId=" +  settingsCon.defaultIds.get(SAVED_FACULTY)
                            + "&departmentId=" + settingsCon.defaultIds.get(SAVED_DEPARTMENT)
                            + "&course=" + settingsCon.defaultIds.get(SAVED_COURSE);
                    jsString = HttpConnectionHelper.doConnectionAction(connectionURL, timeoutSec);
                    mainObject = parser.parse(jsString).getAsJsonObject();
                    items = mainObject.getAsJsonArray("items");
                    for (JsonElement it : items) {
                        JsonObject itObject = it.getAsJsonObject();
                        int id = itObject.get("id").getAsInt();
                        String value = itObject.get("title").getAsString().toLowerCase();
                        settingsCon.groups.put(id, value);
                    }
                    // Get default value for group
                    saved = sPref.getString(SAVED_GROUP, null);
                    curId = tryGetIdByName(saved, settingsCon.groups);
                    defId = curId == -1 ?
                            (settingsCon.groups.isEmpty() ? -1
                                    : settingsCon.groups.keySet().iterator().next().intValue())
                            : curId;
                    settingsCon.defaultIds.put(SAVED_GROUP, defId);

                } catch (Exception e) {

                    return null;
                }

                return settingsCon;
            }
            @Override
            protected void onPostExecute(SettingsContainer settings) {
                super.onPostExecute(settings);

                SettingsActivity sActivity = wrActivity.get();

                if (sActivity != null) {
                    if (settings == null) {
                        Toast.makeText(sActivity, sActivity.getString(R.string.toast_server_not_responding),
                                Toast.LENGTH_LONG).show();
                        settings = new SettingsContainer();
                        SharedPreferences sPref = sActivity.getPreferences(MODE_PRIVATE);

                        String saved = sPref.getString(SAVED_FACULTY, null);
                        if (saved != null) settings.faculties.put(-2, saved);
                        settings.defaultIds.put(SAVED_FACULTY, 0);

                        saved = sPref.getString(SAVED_DEPARTMENT, null);
                        if (saved != null) settings.departments.put(-2, saved);
                        settings.defaultIds.put(SAVED_DEPARTMENT, 0);

                        int savedC = sPref.getInt(SAVED_COURSE, 0);
                        if (savedC != -1) settings.defaultIds.put(SAVED_COURSE, savedC);

                        saved = sPref.getString(SAVED_GROUP, null);
                        if (saved != null) settings.groups.put(-2, saved);
                        settings.defaultIds.put(SAVED_GROUP, 0);

                        initSpinners(sActivity, settings);
                    } else {
                        initSpinners(sActivity, settings);
                    }
                }
                progressDialog.dismiss();
            }
        }.execute(sActivity, pDialog);
    }

    public static void asyncGroupSettingsLoad(SettingsActivity sActivity, ProgressDialog pDialog) {
        Spinner spFac = (Spinner) sActivity.findViewById(R.id.spinner_faculty);
        Spinner spDep = (Spinner) sActivity.findViewById(R.id.spinner_department);
        Spinner spCourse = (Spinner) sActivity.findViewById(R.id.spinner_course);

        HashMap<String, Object> item = (HashMap<String, Object>) spFac.getSelectedItem();
        if (item == null)
        {
            pDialog.dismiss();
            return;
        }
        int idFac = (int)item.get(ATTRIBUTE_NAME_ID);
        item = (HashMap<String, Object>) spDep.getSelectedItem();
        if (item == null)
        {
            pDialog.dismiss();
            return;
        }
        int idDep = (int)item.get(ATTRIBUTE_NAME_ID);
        item = (HashMap<String, Object>) spCourse.getSelectedItem();
        if (item == null)
        {
            pDialog.dismiss();
            return;
        }
        int idCourse = (int)item.get(ATTRIBUTE_NAME_ID);

        new AsyncTask<Object, Void, SettingsContainer>() {
            private WeakReference<SettingsActivity> wrActivity;
            private ProgressDialog progressDialog;


            @Override
            protected SettingsContainer doInBackground(Object... params) {
                if (params[0] == null || params[1] == null)
                    throw new NullPointerException();
                SettingsActivity sActivity = (SettingsActivity)params[0];
                if (sActivity == null)
                    throw new NullPointerException();
                wrActivity = new WeakReference(sActivity);
                progressDialog = (ProgressDialog)params[1];
                SettingsContainer settingsCon = new SettingsContainer();

                try {
                    SharedPreferences sPref = sActivity.getPreferences(MODE_PRIVATE);
                    int timeoutSec = sActivity.getResources()
                            .getInteger(R.integer.server_response_timeout_sec);
                    JsonParser parser = new JsonParser();

                    if (params[2] == null || params[3] == null || params[4] == null)
                        return null;
                    settingsCon.defaultIds.put(SAVED_FACULTY, (Integer) params[2]);
                    settingsCon.defaultIds.put(SAVED_DEPARTMENT, (Integer) params[3]);
                    settingsCon.defaultIds.put(SAVED_COURSE, (Integer) params[4]);

                    // Get groups
                    String connectionURL = sActivity.getString(R.string.api_groups)
                            + "?facultyId=" +  settingsCon.defaultIds.get(SAVED_FACULTY)
                            + "&departmentId=" + settingsCon.defaultIds.get(SAVED_DEPARTMENT)
                            + "&course=" + settingsCon.defaultIds.get(SAVED_COURSE);
                    String jsString = HttpConnectionHelper.doConnectionAction(connectionURL, timeoutSec);
                    JsonObject mainObject = parser.parse(jsString).getAsJsonObject();
                    JsonArray items = mainObject.getAsJsonArray("items");
                    for (JsonElement it : items) {
                        JsonObject itObject = it.getAsJsonObject();
                        int id = itObject.get("id").getAsInt();
                        String value = itObject.get("title").getAsString().toLowerCase();
                        settingsCon.groups.put(id, value);
                    }
                    // Get default value for group
                    String saved = sPref.getString(SAVED_GROUP, null);
                    Integer curId = tryGetIdByName(saved, settingsCon.groups);
                    Integer defId = curId == -1 ?
                            (settingsCon.groups.isEmpty() ? -1
                                    : settingsCon.groups.keySet().iterator().next().intValue())
                            : curId;
                    settingsCon.defaultIds.put(SAVED_GROUP, defId);

                } catch (Exception e) {
                    return null;
                }

                return settingsCon;
            }
            @Override
            protected void onPostExecute(SettingsContainer settings) {
                super.onPostExecute(settings);

                SettingsActivity sActivity = wrActivity.get();

                if (sActivity != null) {

                    if (settings == null) {
                        Toast.makeText(sActivity, sActivity.getString(R.string.toast_server_not_responding),
                                Toast.LENGTH_LONG).show();
                        SharedPreferences sPref = sActivity.getPreferences(MODE_PRIVATE);
                        String saved = sPref.getString(SAVED_GROUP, null);
                        settings = new SettingsContainer();

                        if (saved != null) settings.groups.put(-2, saved);
                        settings.defaultIds.put(SAVED_GROUP, 0);

                        // Init group spinner
                        ArrayList<Map<String, Object>> data = new ArrayList();
                        Map<String, Object> m;
                        int position = 0;
                        int defPosition = 0;
                        if (settings.defaultIds.get(SAVED_GROUP) == -1)
                            settings.groups.put(-1, sActivity.getString(R.string.on_group_list_empty));
                        for (Map.Entry<Integer, String> item : settings.groups.entrySet())
                        {
                            if (settings.defaultIds.get(SAVED_GROUP).equals(item.getKey()))
                                defPosition = position;

                            m = new HashMap();
                            m.put(ATTRIBUTE_NAME_VALUE, item.getValue());
                            m.put(ATTRIBUTE_NAME_ID, item.getKey());
                            data.add(m);
                            ++position;
                        }
                        String[] from = { ATTRIBUTE_NAME_VALUE, ATTRIBUTE_NAME_ID };
                        int[] to = { R.id.spinner_item_value, R.id.spinner_item_id };
                        SimpleAdapter adapter = new SimpleAdapter(sActivity, data, R.layout.spinner_dropdown_item, from, to);

                        Spinner spinner = (Spinner) sActivity.findViewById(R.id.spinner_group);
                        spinner.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        spinner.setSelection(defPosition);

                    } else {
                        // Init group spinner
                        ArrayList<Map<String, Object>> data = new ArrayList();
                        Map<String, Object> m;
                        int position = 0;
                        int defPosition = 0;
                        if (settings.defaultIds.get(SAVED_GROUP) == -1)
                            settings.groups.put(-1, sActivity.getString(R.string.on_group_list_empty));
                        for (Map.Entry<Integer, String> item : settings.groups.entrySet())
                        {
                            if (settings.defaultIds.get(SAVED_GROUP).equals(item.getKey()))
                                defPosition = position;

                            m = new HashMap();
                            m.put(ATTRIBUTE_NAME_VALUE, item.getValue());
                            m.put(ATTRIBUTE_NAME_ID, item.getKey());
                            data.add(m);
                            ++position;
                        }
                        String[] from = { ATTRIBUTE_NAME_VALUE, ATTRIBUTE_NAME_ID };
                        int[] to = { R.id.spinner_item_value, R.id.spinner_item_id };
                        SimpleAdapter adapter = new SimpleAdapter(sActivity, data, R.layout.spinner_dropdown_item, from, to);

                        Spinner spinner = (Spinner) sActivity.findViewById(R.id.spinner_group);
                        spinner.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        spinner.setSelection(defPosition);
                    }
                }

                progressDialog.dismiss();
            }
        }
                .execute(sActivity, pDialog, idFac, idDep, idCourse);
    }

    public static void asyncReturnStudentSchedule(SettingsActivity sActivity, ProgressDialog pDialog) {
        Spinner spGr = (Spinner) sActivity.findViewById(R.id.spinner_group);

        HashMap<String, Object> item = (HashMap<String, Object>) spGr.getSelectedItem();
        if (item == null)
        {
            pDialog.dismiss();
            return;
        }
        int idGr = (int)item.get(ATTRIBUTE_NAME_ID);
        if (idGr == -1)
        {
            pDialog.dismiss();
            Toast.makeText(sActivity, sActivity.getString(R.string.toast_group_empty),
                    Toast.LENGTH_LONG).show();
            return;
        }

        new AsyncTask<Object, Void, String>() {
            private WeakReference<SettingsActivity> wrActivity;
            private ProgressDialog progressDialog;

            @Override
            protected String doInBackground(Object... params) {
                if (params[0] == null || params[1] == null)
                    throw new NullPointerException();
                SettingsActivity sActivity = (SettingsActivity)params[0];
                wrActivity = new WeakReference(sActivity);
                progressDialog = (ProgressDialog)params[1];
                String jsSchedule = null;

                try {
                    if (params[2] == null)
                        return null;
                    int idGr = (Integer) params[2];

                    int timeoutSec = sActivity.getResources()
                            .getInteger(R.integer.server_response_timeout_sec);
                    JsonParser parser = new JsonParser();

                    Calendar c = GregorianCalendar.getInstance(Locale.getDefault());
                    DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                    c.add(Calendar.DATE, -3);
                    String startDate = df.format(c.getTime());
                    c.add(Calendar.DATE, sActivity.getResources().getInteger(R.integer.schedule_max_days));
                    String endDate = df.format(c.getTime());

                    // Get schedule
                    String connectionURL = sActivity.getString(R.string.api_schedule)
                            + "?groupId=" +  idGr
                            + "&dateStart=" + startDate
                            + "&dateEnd=" + endDate;
                    jsSchedule = HttpConnectionHelper.doConnectionAction(connectionURL, timeoutSec);
                    if (jsSchedule == null)
                        return null;

                } catch (Exception e) {

                    return null;
                }

                return jsSchedule;
            }
            @Override
            protected void onPostExecute(String jsSchedule) {
                super.onPostExecute(jsSchedule);

                SettingsActivity sActivity = wrActivity.get();

                if (sActivity != null) {
                    if (jsSchedule == null) {
                        Toast.makeText(sActivity, sActivity.getString(R.string.toast_server_not_responding),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Intent intent= new Intent();
                        intent.putExtra(MainActivity.SAVED_SCHEDULE, jsSchedule);
                        intent.putExtra(MainActivity.SAVED_IS_FOR_STUDENT,
                                sActivity.IsScheduleForStudent);
                        sActivity.setResult(MainActivity.RESULT_OK, intent);
                        sActivity.finish();
                    }
                }
                progressDialog.dismiss();
            }
        }.execute(sActivity, pDialog, idGr);
    }


    private static int tryGetIdByName(String name, Map<Integer, String> map) {
        if (name == null)
            return -1;
        Set<Map.Entry<Integer, String>> set = map.entrySet();

        for (Entry<Integer, String> me : set) {
            if (me.getValue().equals(name))
                return me.getKey();
        }

        return -1;
    }

    private static void initSpinners(SettingsActivity sActivity, SettingsContainer settings) {
        // Init faculty spinner
        ArrayList<Map<String, Object>> data = new ArrayList();
        Map<String, Object> m;
        int position = 0;
        int defPosition = 0;
        for (Map.Entry<Integer, String> item : settings.faculties.entrySet())
        {
            if (settings.defaultIds.get(SAVED_FACULTY).equals(item.getKey()))
                defPosition = position;

            m = new HashMap();
            m.put(ATTRIBUTE_NAME_VALUE, item.getValue());
            m.put(ATTRIBUTE_NAME_ID, item.getKey());
            data.add(m);
            ++position;
        }

        String[] from = { ATTRIBUTE_NAME_VALUE, ATTRIBUTE_NAME_ID };
        int[] to = { R.id.spinner_item_value, R.id.spinner_item_id };
        SimpleAdapter adapter = new SimpleAdapter(sActivity, data, R.layout.spinner_dropdown_item, from, to);

        Spinner spinner = (Spinner) sActivity.findViewById(R.id.spinner_faculty);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        spinner.setSelection(defPosition, false);

        // Init department spinner
        data = new ArrayList();
        position = 0;
        defPosition = 0;
        for (Map.Entry<Integer, String> item : settings.departments.entrySet())
        {
            if (settings.defaultIds.get(SAVED_DEPARTMENT).equals(item.getKey()))
                defPosition = position;

            m = new HashMap();
            m.put(ATTRIBUTE_NAME_VALUE, item.getValue());
            m.put(ATTRIBUTE_NAME_ID, item.getKey());
            data.add(m);
            ++position;
        }
        adapter = new SimpleAdapter(sActivity, data, R.layout.spinner_dropdown_item, from, to);

        spinner = (Spinner) sActivity.findViewById(R.id.spinner_department);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        spinner.setSelection(defPosition, false);

        // Init course spinner
        data = new ArrayList();
        position = 0;
        defPosition = 0;
        for (Integer item : settings.course)
        {
            if (settings.defaultIds.get(SAVED_COURSE).equals(item))
                defPosition = position;

            m = new HashMap();
            m.put(ATTRIBUTE_NAME_VALUE, item.toString());
            m.put(ATTRIBUTE_NAME_ID, item);
            data.add(m);
            ++position;
        }
        adapter = new SimpleAdapter(sActivity, data, R.layout.spinner_dropdown_item, from, to);

        spinner = (Spinner) sActivity.findViewById(R.id.spinner_course);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        spinner.setSelection(defPosition, false);

        // Init group spinner
        data = new ArrayList();
        position = 0;
        defPosition = 0;
        if (settings.defaultIds.get(SAVED_GROUP) == -1)
            settings.groups.put(-1, sActivity.getString(R.string.on_group_list_empty));
        for (Map.Entry<Integer, String> item : settings.groups.entrySet())
        {
            if (settings.defaultIds.get(SAVED_GROUP).equals(item.getKey()))
                defPosition = position;

            m = new HashMap();
            m.put(ATTRIBUTE_NAME_VALUE, item.getValue());
            m.put(ATTRIBUTE_NAME_ID, item.getKey());
            data.add(m);
            ++position;
        }
        adapter = new SimpleAdapter(sActivity, data, R.layout.spinner_dropdown_item, from, to);

        spinner = (Spinner) sActivity.findViewById(R.id.spinner_group);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        spinner.setSelection(defPosition, false);
    }
}
