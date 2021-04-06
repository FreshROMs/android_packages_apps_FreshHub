package de.dlyt.yanndroid.freshapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.HashMap;

import de.dlyt.yanndroid.freshapp.R;

import static de.dlyt.yanndroid.freshapp.utils.Tools.shell;

public class Screen_Resolution extends AppCompatActivity {

    public static String PREF_NAME = "ScreenResolutionSettings";
    public static String SCREEN_RESOLUTION = "device_screen_resolution";

    int resolution;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen__resolution);

        initToolbar();
        settilte("Screen resolution");

        sharedPreferences = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        resolution = sharedPreferences.getInt(SCREEN_RESOLUTION, R.id.resolution_high);

        RadioGroup resolution_radiogroup = findViewById(R.id.resolution_radiogroup);
        MaterialRadioButton resolution_low = findViewById(R.id.resolution_low);
        MaterialRadioButton resolution_medium = findViewById(R.id.resolution_medium);
        MaterialRadioButton resolution_high = findViewById(R.id.resolution_high);

        TextView resolution_summary = findViewById(R.id.resolution_summary);

        resolution_radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                resolution_low.setTypeface(Typeface.DEFAULT);
                resolution_medium.setTypeface(Typeface.DEFAULT);
                resolution_high.setTypeface(Typeface.DEFAULT);
                MaterialRadioButton resolution_tosetBold = findViewById(checkedId);
                resolution_tosetBold.setTypeface(Typeface.DEFAULT_BOLD);
                resolution = checkedId;
                switch (checkedId){
                    case R.id.resolution_low:
                        resolution_summary.setText(R.string.low_resolution_summary);
                        break;
                    case R.id.resolution_medium:
                        resolution_summary.setText(R.string.medium_resolution_summary);
                        break;
                    case R.id.resolution_high:
                        resolution_summary.setText(R.string.high_resolution_summary);
                        break;
                }
            }
        });
        resolution_radiogroup.check(resolution);


        MaterialButton resolution_apply = findViewById(R.id.resolution_apply);
        resolution_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putInt(SCREEN_RESOLUTION, resolution).commit();
                switch (resolution){
                    case R.id.resolution_low:
                        setResolution(getString(R.string.low_resolution_value), getString(R.string.low_resolution_density));
                        break;
                    case R.id.resolution_medium:
                        setResolution(getString(R.string.medium_resolution_value), getString(R.string.medium_resolution_density));
                        break;
                    case R.id.resolution_high:
                        setResolution(getString(R.string.high_resolution_value), getString(R.string.high_resolution_density));
                        break;
                }
            }
        });

    }

    public void setResolution(String resolution, String density){
        shell("pm grant de.dlyt.yanndroid.freshapp android.permission.WRITE_SECURE_SETTINGS", true);
        shell("su -c wm size "+ resolution, true);
        shell("su -c wm density "+ density, true);
        shell("su -c settings put secure sysui_rounded_size 1", true);
        shell("su -c settings put secure sysui_rounded_size 0", true);
    }

    public void initToolbar() {
        /** Def */
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppBarLayout AppBar = findViewById(R.id.app_bar);

        TextView expanded_title = findViewById(R.id.expanded_title);
        TextView expanded_subtitle = findViewById(R.id.expanded_subtitle);
        TextView collapsed_title = findViewById(R.id.collapsed_title);

        /** 1/3 of the Screen */
        ViewGroup.LayoutParams layoutParams = AppBar.getLayoutParams();
        layoutParams.height = (int) ((double) this.getResources().getDisplayMetrics().heightPixels / 2.6);


        /** Collapsing */
        AppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float percentage = (AppBar.getY() / AppBar.getTotalScrollRange());
                expanded_title.setAlpha(1 - (percentage * 2 * -1));
                expanded_subtitle.setAlpha(1 - (percentage * 2 * -1));
                collapsed_title.setAlpha(percentage * -1);
            }
        });
        AppBar.setExpanded(false);

        /**Back*/
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void settilte(String title) {
        TextView expanded_title = findViewById(R.id.expanded_title);
        TextView collapsed_title = findViewById(R.id.collapsed_title);
        expanded_title.setText(title);
        collapsed_title.setText(title);
    }

}
