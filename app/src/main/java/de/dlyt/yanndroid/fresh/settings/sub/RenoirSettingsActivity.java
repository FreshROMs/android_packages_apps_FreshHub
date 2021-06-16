package de.dlyt.yanndroid.fresh.settings.sub;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.renoir.RenoirService;
import de.dlyt.yanndroid.samsung.SwitchBar;
import de.dlyt.yanndroid.samsung.layout.ToolbarLayout;
import io.tensevntysevn.fresh.ExperienceUtils;

public class RenoirSettingsActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    boolean mRenoirEnabled;
    boolean mRenoirLsWallpaper;

    SwitchBar mRenoirSwitchBar;
    SwitchMaterial mRenoirLsSwitch;

    Handler handler;
    LinearLayout renoirLsSwitchLayout;

    public static void setLayoutEnabled(LinearLayout view, boolean enable) {
        view.setEnabled(enable);
        view.setClickable(enable);
        view.setFocusable(enable);
        view.setAlpha(enable ? 1f : 0.7f);
    }

    @SuppressLint("ApplySharedPref")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zest_renoir_settings);

        mContext = this;
        handler = new Handler(Looper.getMainLooper());

        mRenoirEnabled = RenoirService.getRenoirEnabled(mContext);
        mRenoirLsWallpaper = RenoirService.getColorBasedOnLock(mContext);

        mRenoirSwitchBar = findViewById(R.id.zest_renoir_switch_bar);

        mRenoirLsSwitch = findViewById(R.id.switch_renoir_lock_screen);

        TextView renoirDescription = findViewById(R.id.renoir_description_text);
        renoirLsSwitchLayout = findViewById(R.id.switch_renoir_lock_screen_layout);

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        setSupportActionBar(toolbarLayout.getToolbar());
        toolbarLayout.setExpanded(false, false);
        toolbarLayout.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (ExperienceUtils.isGalaxyThemeApplied(mContext)) {
            mRenoirLsSwitch.setEnabled(false);
            setLayoutEnabled(renoirLsSwitchLayout, false);
            renoirDescription.setText(getString(R.string.renoir_settings_desc_unavailable));
        } else {
            setLayoutEnabled(renoirLsSwitchLayout, mRenoirEnabled);
            mRenoirLsSwitch.setEnabled(mRenoirLsWallpaper);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        updatePreferences();
        super.onResume();
    }

    public void toggleRenoirSwitch(View v) {
        mRenoirLsSwitch.toggle();
    }

    private void updatePreferences() {
        mRenoirEnabled = RenoirService.getRenoirEnabled(mContext);
        mRenoirLsWallpaper = RenoirService.getColorBasedOnLock(mContext);

        mRenoirSwitchBar.setChecked(mRenoirEnabled);
        mRenoirLsSwitch.setChecked(mRenoirLsWallpaper);

        mRenoirSwitchBar.addOnSwitchChangeListener((buttonView, isChecked) -> {
            if (!(isChecked == mRenoirEnabled)) {
                buttonView.setChecked(isChecked);
                mRenoirEnabled = isChecked;

                mRenoirSwitchBar.setProgressBarVisible(true);
                mRenoirSwitchBar.setEnabled(false);
                setLayoutEnabled(renoirLsSwitchLayout, false);

                RenoirService.setRenoirEnabled(mContext, isChecked);

                handler.postDelayed(() -> {
                    mRenoirSwitchBar.setProgressBarVisible(false);
                    mRenoirSwitchBar.setEnabled(true);
                    setLayoutEnabled(renoirLsSwitchLayout, isChecked);
                }, 1500);
            }
        });

        mRenoirLsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!(isChecked == mRenoirLsWallpaper)) {
                mRenoirLsWallpaper = isChecked;
                mRenoirSwitchBar.setProgressBarVisible(true);
                mRenoirSwitchBar.setEnabled(false);
                setLayoutEnabled(renoirLsSwitchLayout, false);
                RenoirService.setColorBasedOnLock(mContext, isChecked);

                handler.postDelayed(() -> {
                    mRenoirSwitchBar.setProgressBarVisible(false);
                    mRenoirSwitchBar.setEnabled(true);
                    setLayoutEnabled(renoirLsSwitchLayout, true);
                }, 1500);
            }
        });
    }
}