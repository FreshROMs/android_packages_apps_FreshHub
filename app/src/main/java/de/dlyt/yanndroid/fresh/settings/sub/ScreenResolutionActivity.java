package de.dlyt.yanndroid.fresh.settings.sub;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.utils.Tools;
import de.dlyt.yanndroid.samsung.layout.ToolbarLayout;
import io.tensevntysevn.fresh.ExperienceUtils;

public class ScreenResolutionActivity extends AppCompatActivity {

    public static String PREF_NAME = "fresh_system_settings";
    public static String SCREEN_RESOLUTION = "device_screen_resolution_int";
    public static String RESTRICTED_API = "device_restricted_api";

    int mResolution;
    int mResolutionId;
    int mCurrentResolution;
    int mSelectedResolution = 2;

    private Context mContext;
    private static ExecutorService mExecutor;
    private SharedPreferences sharedPreferences;
    RadioGroup mResGroup;
    MaterialButton mResApplyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        mExecutor = Executors.newSingleThreadExecutor();
        sharedPreferences = getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
        setContentView(R.layout.activity_zest_screen_resolution);

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setExpanded(false, false);
        setSupportActionBar(toolbarLayout.getToolbar());
        toolbarLayout.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ExperienceUtils.checkDefaultApiSetting(this);
        ExperienceUtils.getRealScreenWidth(this, Tools.getActivity(this));

        TextView resolution_summary = findViewById(R.id.resolution_summary);
        mResApplyButton = findViewById(R.id.resolution_apply);

        mResGroup = findViewById(R.id.resolution_radiogroup);
        MaterialRadioButton resolution_low = findViewById(R.id.resolution_low);
        MaterialRadioButton resolution_medium = findViewById(R.id.resolution_medium);
        MaterialRadioButton resolution_high = findViewById(R.id.resolution_high);

        resolution_low.setTypeface(Typeface.DEFAULT);
        resolution_medium.setTypeface(Typeface.DEFAULT);
        resolution_high.setTypeface(Typeface.DEFAULT);

        mResolution = Settings.System.getInt(getContentResolver(), SCREEN_RESOLUTION, mSelectedResolution);
        mCurrentResolution = mResolution;

        mResGroup.setOnCheckedChangeListener((group, checkedId) -> {
            resolution_low.setTypeface(Typeface.DEFAULT);
            resolution_medium.setTypeface(Typeface.DEFAULT);
            resolution_high.setTypeface(Typeface.DEFAULT);

            if (checkedId == R.id.resolution_low) {
                resolution_summary.setText(R.string.low_resolution_summary);
                resolution_low.setTypeface(Typeface.DEFAULT_BOLD);
                mResolution = 0;
            } else if (checkedId == R.id.resolution_medium) {
                resolution_summary.setText(R.string.medium_resolution_summary);
                resolution_medium.setTypeface(Typeface.DEFAULT_BOLD);
                mResolution = 1;
            } else {
                resolution_summary.setText(R.string.high_resolution_summary);
                resolution_high.setTypeface(Typeface.DEFAULT_BOLD);
                mResolution = 2;
            }

            mResApplyButton.setEnabled(mCurrentResolution != mResolution);
        });

        switch (mResolution) {
            case 0:
                mResolutionId = R.id.resolution_low;
                break;
            case 1:
                mResolutionId = R.id.resolution_medium;
                break;
            default:
                mResolutionId = R.id.resolution_high;
        }

        mResGroup.check(mResolutionId);
        mResApplyButton.setEnabled(false);

        mResApplyButton.setOnClickListener(v -> {
            Settings.System.putInt(getContentResolver(), SCREEN_RESOLUTION, mResolution);
            mResApplyButton.setEnabled(false);
            mCurrentResolution = mResolution;
            ExperienceUtils.checkDefaultApiSetting(mContext);
            try {
                ExperienceUtils.setBypassBlacklist(mContext, true);

                switch (mResolution) {
                    case 0:
                        setResolution(getString(R.string.low_resolution_value), getString(R.string.low_resolution_density));
                        break;
                    case 1:
                        setResolution(getString(R.string.medium_resolution_value), getString(R.string.medium_resolution_density));
                        break;
                    case 2:
                        setResolution("reset", "reset");
                }

                // Re-lock APIs for security
                ExperienceUtils.setBypassBlacklist(mContext, false);

                mExecutor.execute(() -> {
                    ExperienceUtils.stopPackage(mContext, "com.sec.android.app.launcher");
                    ExperienceUtils.stopPackage(mContext, "com.samsung.android.honeyboard");
                });
            } catch (Exception e) {
                Log.e("ScreenResolutionService", "Fail!", e);
            }
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mResolution = ExperienceUtils.getRealScreenWidth(this, Tools.getActivity(this));
        mCurrentResolution = mResolution;
        mResGroup = findViewById(R.id.resolution_radiogroup);

        switch (mResolution) {
            case 0:
                mResGroup.check(R.id.resolution_low);
                break;
            case 1:
                mResGroup.check(R.id.resolution_medium);
                break;
            default:
                mResGroup.check(R.id.resolution_high);
        }

        mResApplyButton.setEnabled(false);
    }

    public static int getResolutionInt(Context context) {
        return Settings.System.getInt(context.getContentResolver(), SCREEN_RESOLUTION, 2);
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

}
