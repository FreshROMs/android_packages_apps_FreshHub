package de.dlyt.yanndroid.freshapp.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.Scanner;

import de.dlyt.yanndroid.freshapp.R;

public class ScreenResolutionSettings extends AppCompatActivity {

    public static String PREF_NAME = "ScreenResolutionSettings";
    public static String SCREEN_RESOLUTION = "device_screen_resolution";
    public static String RESTRICTED_API = "device_restricted_api";

    int resolution;
    int default_api_setting;
    int current_resolution;

    private Context mContext;
    private SharedPreferences sharedPreferences;

    public static void setBypassBlacklist(Context context, boolean bool) {
        /* List of Global settings that allow blacklisted APIs to be called */
        String[] blacklistGlobalSettings = {
                "hidden_api_policy",
                "hidden_api_policy_pre_p_apps",
                "hidden_api_policy_p_apps"
        };

        SharedPreferences sharedPreferencesBp = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        int default_restricted_api_setting = sharedPreferencesBp.getInt(RESTRICTED_API, 0);

        if (bool) {
            for (String setting : blacklistGlobalSettings) {
                Settings.Global.putInt(context.getContentResolver(), setting, 1);
            }
        } else {
            for (String setting : blacklistGlobalSettings) {
                Settings.Global.putInt(context.getContentResolver(), setting, default_restricted_api_setting);
            }
        }
    }

    @SuppressLint("PrivateApi")
    private static Object getWindowManagerService() throws Exception {
        return Class.forName("android.view.WindowManagerGlobal")
                .getMethod("getWindowManagerService")
                .invoke(null);
    }

    @SuppressLint("PrivateApi")
    private static void setResolution(String wmSize, String wmDensity) throws Exception {
        final int USER_CURRENT_OR_SELF = -3;

        if (wmSize.equals("reset")) {
            Class.forName("android.view.IWindowManager")
                    .getMethod("clearForcedDisplaySize", int.class)
                    .invoke(getWindowManagerService(), Display.DEFAULT_DISPLAY);
        } else {
            Scanner scanner = new Scanner(wmSize);
            scanner.useDelimiter("x");

            int width = scanner.nextInt();
            int height = scanner.nextInt();

            scanner.close();

            Class.forName("android.view.IWindowManager")
                    .getMethod("setForcedDisplaySize", int.class, int.class, int.class)
                    .invoke(getWindowManagerService(), Display.DEFAULT_DISPLAY, width, height);
        }

        if (wmDensity.equals("reset")) {
            Class.forName("android.view.IWindowManager")
                    .getMethod("clearForcedDisplayDensityForUser", int.class, int.class)
                    .invoke(getWindowManagerService(), Display.DEFAULT_DISPLAY, USER_CURRENT_OR_SELF);
        } else {
            int density = Integer.parseInt(wmDensity);

            Class.forName("android.view.IWindowManager")
                    .getMethod("setForcedDisplayDensityForUser", int.class, int.class, int.class)
                    .invoke(getWindowManagerService(), Display.DEFAULT_DISPLAY, density, USER_CURRENT_OR_SELF);
        }
    }

    private void checkDefaultApiSetting() {
        try {
            default_api_setting = Settings.Global.getInt(mContext.getContentResolver(), "hidden_api_policy");
            sharedPreferences.edit().putInt(RESTRICTED_API, default_api_setting).commit();
        } catch (Exception e) { /* Fail */ }
        ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_resolution);

        initToolbar();
        settilte("Screen resolution");

        sharedPreferences = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        checkDefaultApiSetting();

        RadioGroup resolution_radiogroup = findViewById(R.id.resolution_radiogroup);
        MaterialButton resolution_apply = findViewById(R.id.resolution_apply);
        MaterialRadioButton resolution_low = findViewById(R.id.resolution_low);
        MaterialRadioButton resolution_medium = findViewById(R.id.resolution_medium);
        MaterialRadioButton resolution_high = findViewById(R.id.resolution_high);

        resolution = sharedPreferences.getInt(SCREEN_RESOLUTION, R.id.resolution_high);

        TextView resolution_summary = findViewById(R.id.resolution_summary);

        resolution_radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                resolution_low.setTypeface(Typeface.DEFAULT);
                resolution_medium.setTypeface(Typeface.DEFAULT);
                resolution_high.setTypeface(Typeface.DEFAULT);

                resolution_apply.setEnabled(current_resolution != checkedId);
                resolution = checkedId;

                switch (checkedId) {
                    case R.id.resolution_low:
                        resolution_summary.setText(R.string.low_resolution_summary);
                        resolution_low.setTypeface(Typeface.DEFAULT_BOLD);
                        break;
                    case R.id.resolution_medium:
                        resolution_summary.setText(R.string.medium_resolution_summary);
                        resolution_medium.setTypeface(Typeface.DEFAULT_BOLD);
                        break;
                    case R.id.resolution_high:
                        resolution_summary.setText(R.string.high_resolution_summary);
                        resolution_high.setTypeface(Typeface.DEFAULT_BOLD);
                        break;
                }
            }
        });
        switch (resolution) { /**seems unnecessary but else the app crashes at the first time*/
            case R.id.resolution_high:
                resolution = R.id.resolution_high;
                break;
            case R.id.resolution_medium:
                resolution = R.id.resolution_medium;
                break;
            case R.id.resolution_low:
                resolution = R.id.resolution_low;
                break;
            default:
                resolution = R.id.resolution_high;
        }
        current_resolution = resolution;
        resolution_radiogroup.check(resolution);
        resolution_apply.setEnabled(false);

        resolution_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putInt(SCREEN_RESOLUTION, resolution).commit();
                resolution_apply.setEnabled(false);
                current_resolution = resolution;
                checkDefaultApiSetting(); // Re-check default value before setting
                try {
                    setBypassBlacklist(mContext, true);

                    switch (resolution) {
                        case R.id.resolution_low:
                            setResolution(getString(R.string.low_resolution_value), getString(R.string.low_resolution_density));
                            break;
                        case R.id.resolution_medium:
                            setResolution(getString(R.string.medium_resolution_value), getString(R.string.medium_resolution_density));
                            break;
                        case R.id.resolution_high:
                            setResolution("reset", "reset");
                            break;
                    }

                    // Re-lock APIs for security
                    setBypassBlacklist(mContext, false);

                } catch (Exception e) {
                    Log.e("ScreenResolutionService", "Fail!", e);
                }
            }
        });

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
