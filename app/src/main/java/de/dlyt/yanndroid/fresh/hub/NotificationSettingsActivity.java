package de.dlyt.yanndroid.fresh.hub;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.hub.utils.Preferences;
import de.dlyt.yanndroid.fresh.utils.JobScheduler;
import de.dlyt.yanndroid.samsung.SwitchBar;
import de.dlyt.yanndroid.samsung.layout.ToolbarLayout;

public class NotificationSettingsActivity extends AppCompatActivity {

    public static void setLayoutEnabled(View view, boolean enable) {
        view.setEnabled(enable);
        view.setClickable(enable);
        view.setFocusable(enable);
        view.setAlpha(enable ? 1f : 0.7f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        Context mContext = this;

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setExpanded(false, false);
        setSupportActionBar(toolbarLayout.getToolbar());
        toolbarLayout.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        SwitchBar notifSwitch = findViewById(R.id.switch_notifications);
        SwitchMaterial dataSaver = findViewById(R.id.switch_data_saver);

        notifSwitch.setChecked(Preferences.getBackgroundService(mContext));
        dataSaver.setChecked(Preferences.getBackgroundDownload(mContext));
        dataSaver.setEnabled(Preferences.getBackgroundService(mContext));


        LinearLayout background_options_layout = findViewById(R.id.background_options);
        LinearLayout data_saver_layout = findViewById(R.id.data_saver_layout);

        String[] background_options = getResources().getStringArray(R.array.updater_background_frequency_entries);
        String[] background_values = getResources().getStringArray(R.array.updater_background_frequency_values);
        Spinner background_spinner = findViewById(R.id.background_options_spinner);
        Integer background_selected = Preferences.getBackgroundFrequencyOption(mContext);


        ArrayAdapter<String> background_spinner_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, background_options);
        background_spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        background_spinner.setAdapter(background_spinner_adapter);
        background_spinner_adapter.notifyDataSetChanged();

        background_spinner.setSelection(background_selected);
        final int[] background_spinner_selection = {background_selected};

        setLayoutEnabled(background_options_layout, Preferences.getBackgroundService(mContext));
        setLayoutEnabled(background_spinner, Preferences.getBackgroundService(mContext));
        setLayoutEnabled(data_saver_layout, Preferences.getBackgroundService(mContext));

        background_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int selection, long id) {
                background_spinner_selection[0] = selection;
                String backgroundTime = background_values[selection];
                Preferences.setBackgroundFrequency(mContext, backgroundTime);
                Preferences.setBackgroundFrequencyOption(mContext, selection);
                JobScheduler.setBackgroundCheck(mContext, Preferences.getBackgroundService(mContext));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        notifSwitch.addOnSwitchChangeListener(new SwitchBar.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(SwitchCompat switchCompat, boolean b) {
                Preferences.setBackgroundService(mContext, b);
                JobScheduler.setBackgroundCheck(mContext, Preferences.getBackgroundService(mContext));
                setLayoutEnabled(background_options_layout, b);
                setLayoutEnabled(background_spinner, b);
                setLayoutEnabled(data_saver_layout, b);

                if (Preferences.getBackgroundDownload(mContext)) {
                    dataSaver.setChecked(b);
                }

                dataSaver.setEnabled(b);
            }
        });

        dataSaver.setOnCheckedChangeListener((buttonView, isChecked) -> Preferences.setBackgroundDownload(mContext, isChecked));


    }

    public void toggleAutoUpdateSwitch(View v) {
        SwitchMaterial dataSaver = findViewById(R.id.switch_data_saver);
        dataSaver.toggle();
    }
}