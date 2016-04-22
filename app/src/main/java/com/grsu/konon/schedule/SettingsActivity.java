package com.grsu.konon.schedule;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Toolbar;

import com.grsu.konon.schedule.Helpers.SettingsLoadingHelper;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    public boolean IsScheduleForStudent = true;
    private final String SAVED_TYPE = "saved_type";
    private LinearLayout llForm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        llForm = (LinearLayout) findViewById(R.id.ll_form);
        setTypeSpinner();
        setShowButton();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_menu_refresh:
                updateForm();
                return true;

            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        String value;

        switch (parent.getId()) {
            case R.id.spinner_type:
                editor.putInt(SAVED_TYPE, pos).commit();
                if (pos == 0) showStudentForm();
                else showTeacherForm();
                break;

            case R.id.spinner_faculty:
                Object it = parent.getSelectedItem();
                value = ((HashMap<String, Object>) parent.getSelectedItem())
                        .get(SettingsLoadingHelper.ATTRIBUTE_NAME_VALUE).toString();
                editor.putString(SettingsLoadingHelper.SAVED_FACULTY, value).commit();
                reloadGroupSpinner();
                break;

            case R.id.spinner_department:
                value = ((HashMap<String, Object>) parent.getSelectedItem())
                        .get(SettingsLoadingHelper.ATTRIBUTE_NAME_VALUE).toString();
                editor.putString(SettingsLoadingHelper.SAVED_DEPARTMENT, value).commit();
                reloadGroupSpinner();
                break;

            case R.id.spinner_course:
                value = ((HashMap<String, Object>) parent.getSelectedItem())
                        .get(SettingsLoadingHelper.ATTRIBUTE_NAME_VALUE).toString();
                editor.putInt(SettingsLoadingHelper.SAVED_COURSE, Integer.parseInt(value)).commit();
                reloadGroupSpinner();
                break;

            case R.id.spinner_group:
                value = ((HashMap<String, Object>) parent.getSelectedItem())
                        .get(SettingsLoadingHelper.ATTRIBUTE_NAME_VALUE).toString();
                editor.putString(SettingsLoadingHelper.SAVED_GROUP, value).commit();
                break;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_show:
                tryReturnSchedule();
                break;
        }
    }


    private void setTypeSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner_type);
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        int saved = sPref.getInt(SAVED_TYPE, 0);
        spinner.setSelection(saved);
        spinner.setOnItemSelectedListener(this);
    }

    private void setShowButton() {
        Button button = (Button) findViewById(R.id.button_show);
        button.setOnClickListener(this);
    }

    private void setStudentFormSpinners() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner_faculty);
        spinner.setOnItemSelectedListener(this);
        spinner = (Spinner) findViewById(R.id.spinner_department);
        spinner.setOnItemSelectedListener(this);
        spinner = (Spinner) findViewById(R.id.spinner_course);
        spinner.setOnItemSelectedListener(this);
        spinner = (Spinner) findViewById(R.id.spinner_group);
        spinner.setOnItemSelectedListener(this);
    }

    private void showStudentForm() {
        llForm.removeAllViews();
        IsScheduleForStudent = true;
        LayoutInflater ltInflater = getLayoutInflater();
        ltInflater.inflate(R.layout.form_student, llForm, true);

        ProgressDialog progressDialog = ProgressDialog.show(this,
                getString(R.string.settings_progress_dialog_title),
                getString(R.string.settings_progress_dialog_message), false, false);

        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                setStudentFormSpinners();
            }
        });

        SettingsLoadingHelper.asyncStudentSettingsLoad(this, progressDialog);
    }

    private void showTeacherForm() {
        llForm.removeAllViews();
        IsScheduleForStudent = false;
        Toast.makeText(this, getString(R.string.toast_not_implemented), Toast.LENGTH_LONG)
                .show();
    }

    private void reloadGroupSpinner() {
        ProgressDialog progressDialog = ProgressDialog.show(this,
                getString(R.string.settings_progress_dialog_title),
                getString(R.string.settings_progress_dialog_message), false, false);
        SettingsLoadingHelper.asyncGroupSettingsLoad(this, progressDialog);
    }

    private void tryReturnSchedule() {
        if (haveEmptyValue()) {
            Toast.makeText(this, getString(R.string.toast_value_empty), Toast.LENGTH_LONG).show();
        }
        else {
            if (haveInvalidValue())
                updateForm();

            if (IsScheduleForStudent)
            {
                ProgressDialog progressDialog = ProgressDialog.show(this,
                        getString(R.string.settings_progress_dialog_title),
                        getString(R.string.settings_progress_dialog_message), false, false);

                SettingsLoadingHelper.asyncReturnStudentSchedule(this, progressDialog);
            }
            else
            {
                Toast.makeText(this, getString(R.string.toast_not_implemented), Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private void updateForm() {
        if (IsScheduleForStudent) {
            showStudentForm();
        }
        else {
            showTeacherForm();
        }
    }

    private boolean haveEmptyValue() {
        if (IsScheduleForStudent) {
            Spinner spFac = (Spinner) findViewById(R.id.spinner_faculty);
            Spinner spDep = (Spinner) findViewById(R.id.spinner_department);
            Spinner spGr = (Spinner) findViewById(R.id.spinner_group);

            HashMap<String, Object> itemFac = (HashMap<String, Object>) spFac.getSelectedItem();
            HashMap<String, Object> itemDep = (HashMap<String, Object>) spDep.getSelectedItem();
            HashMap<String, Object> itemGr = (HashMap<String, Object>) spGr.getSelectedItem();

            if (itemFac == null || itemDep == null || itemGr == null)
                return true;

            return false;
        }
        else {
            return false;
        }
    }

    private boolean haveInvalidValue() {
        if (IsScheduleForStudent) {
            try {
                Spinner spFac = (Spinner) findViewById(R.id.spinner_faculty);
                Spinner spDep = (Spinner) findViewById(R.id.spinner_department);
                Spinner spGr = (Spinner) findViewById(R.id.spinner_group);

                HashMap<String, Object> itemFac = (HashMap<String, Object>) spFac.getSelectedItem();
                HashMap<String, Object> itemDep = (HashMap<String, Object>) spDep.getSelectedItem();
                HashMap<String, Object> itemGr = (HashMap<String, Object>) spGr.getSelectedItem();

                int facID = (Integer) itemFac.get(SettingsLoadingHelper.ATTRIBUTE_NAME_ID);
                int depID = (Integer) itemDep.get(SettingsLoadingHelper.ATTRIBUTE_NAME_ID);
                int grID = (Integer) itemGr.get(SettingsLoadingHelper.ATTRIBUTE_NAME_ID);

                if (facID == -2 || depID == -2 || grID == -2)
                    return true;
            }
            catch (Exception ex) {
                return true;
            }

            return false;
        }
        else {
            return false;
        }
    }
}
