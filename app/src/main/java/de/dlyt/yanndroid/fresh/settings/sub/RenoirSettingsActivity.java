package de.dlyt.yanndroid.fresh.settings.sub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.renoir.RenoirService;
import de.dlyt.yanndroid.samsung.ColorPickerDialog;
import de.dlyt.yanndroid.samsung.SwitchBar;
import de.dlyt.yanndroid.samsung.layout.ToolbarLayout;
import io.tensevntysevn.fresh.ExperienceUtils;

public class RenoirSettingsActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    boolean mRenoirEnabled;
    boolean mRenoirLsWallpaper;
    boolean mRenoirCc;

    SwitchBar mRenoirSwitchBar;
    SwitchMaterial mRenoirLsSwitch;
    SwitchMaterial mRenoirCcSwitch;

    Handler handler;
    LinearLayout renoirLsSwitchLayout;
    LinearLayout renoirCcSwitchLayout;

    View renoirCcPickerView;
    int mRenoirCustomColor;

    public static void setLayoutEnabled(View view, boolean enable) {
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
        mRenoirCc = RenoirService.getColorBasedOnCustom(mContext);
        mRenoirCustomColor = RenoirService.getColorForBasedOnCustom(mContext);

        mRenoirSwitchBar = findViewById(R.id.zest_renoir_switch_bar);

        mRenoirLsSwitch = findViewById(R.id.switch_renoir_lock_screen);
        renoirLsSwitchLayout = findViewById(R.id.switch_renoir_lock_screen_layout);

        mRenoirCcSwitch = findViewById(R.id.switch_renoir_custom_color);
        renoirCcSwitchLayout = findViewById(R.id.switch_renoir_custom_color_layout);
        renoirCcPickerView = findViewById(R.id.custom_color_circle);

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        setSupportActionBar(toolbarLayout.getToolbar());
        toolbarLayout.setExpanded(false, false);
        toolbarLayout.setNavigationOnClickListener(v -> onBackPressed());
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

    public void toggleRenoirLsSwitch(View v) {
        mRenoirLsSwitch.toggle();
    }

    public void toggleRenoirCcSwitch(View v) {
        mRenoirCcSwitch.toggle();
    }

    private void updatePreferences() {
        TextView renoirDescription = findViewById(R.id.renoir_description_text);

        if (ExperienceUtils.isGalaxyThemeApplied(mContext)) {
            setLayoutEnabled(renoirLsSwitchLayout, false);
            setLayoutEnabled(renoirCcSwitchLayout, false);

            renoirDescription.setText(getString(R.string.renoir_settings_desc_unavailable));
            mRenoirEnabled = false;
            mRenoirLsWallpaper = false;
            mRenoirCc = false;
        } else if (ExperienceUtils.isLsWallpaperUnavailable(mContext)) {
            setLayoutEnabled(renoirLsSwitchLayout, false);
            setLayoutEnabled(renoirCcSwitchLayout, false);

            mRenoirEnabled = false;
            mRenoirLsWallpaper = false;
            mRenoirCc = false;
        } else {
            mRenoirEnabled = RenoirService.getRenoirEnabled(mContext);
            mRenoirLsWallpaper = RenoirService.getColorBasedOnLock(mContext);
            mRenoirCc = RenoirService.getColorBasedOnCustom(mContext);

            setLayoutEnabled(renoirLsSwitchLayout, mRenoirEnabled && !mRenoirCc);
            setLayoutEnabled(renoirCcSwitchLayout, mRenoirEnabled && !mRenoirLsWallpaper);
        }
        mRenoirCustomColor = RenoirService.getColorForBasedOnCustom(mContext);

        mRenoirSwitchBar.setChecked(mRenoirEnabled);

        mRenoirLsSwitch.setChecked(mRenoirLsWallpaper);
        mRenoirCcSwitch.setChecked(mRenoirCc);
        renoirCcPickerView.setVisibility(mRenoirCc ? View.VISIBLE : View.GONE);

        mRenoirSwitchBar.addOnSwitchChangeListener((buttonView, isChecked) -> {
            if (!(isChecked == mRenoirEnabled)) {
                buttonView.setChecked(isChecked);
                mRenoirEnabled = isChecked;

                mRenoirSwitchBar.setProgressBarVisible(true);
                mRenoirSwitchBar.setEnabled(false);
                setLayoutEnabled(renoirLsSwitchLayout, false);
                setLayoutEnabled(renoirCcSwitchLayout, false);

                RenoirService.setRenoirEnabled(mContext, isChecked);

                handler.postDelayed(() -> {
                    mRenoirSwitchBar.setProgressBarVisible(false);
                    mRenoirSwitchBar.setEnabled(true);
                    setLayoutEnabled(renoirLsSwitchLayout, isChecked && !mRenoirCc);
                    setLayoutEnabled(renoirCcSwitchLayout, isChecked && !mRenoirLsWallpaper);
                }, 1500);
            }
        });

        mRenoirLsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!(isChecked == mRenoirLsWallpaper)) {
                if (isChecked) mRenoirCcSwitch.setChecked(false);

                mRenoirLsWallpaper = isChecked;
                mRenoirSwitchBar.setProgressBarVisible(true);
                mRenoirSwitchBar.setEnabled(false);
                setLayoutEnabled(renoirLsSwitchLayout, false);
                setLayoutEnabled(renoirCcSwitchLayout, false);
                RenoirService.setColorBasedOnLock(mContext, isChecked);

                handler.postDelayed(() -> {
                    mRenoirSwitchBar.setProgressBarVisible(false);
                    mRenoirSwitchBar.setEnabled(true);
                    setLayoutEnabled(renoirLsSwitchLayout, !mRenoirCc);
                    setLayoutEnabled(renoirCcSwitchLayout, !isChecked);
                }, 1500);
            }
        });

        mRenoirCcSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!(isChecked == mRenoirCc)) {
                if (isChecked) mRenoirLsSwitch.setChecked(false);

                mRenoirCc = isChecked;
                mRenoirSwitchBar.setProgressBarVisible(true);
                mRenoirSwitchBar.setEnabled(false);
                setLayoutEnabled(renoirLsSwitchLayout, false);
                setLayoutEnabled(renoirCcSwitchLayout, false);
                RenoirService.setColorBasedOnCustom(mContext, isChecked, mRenoirCustomColor);

                renoirCcPickerView.setVisibility(isChecked ? View.VISIBLE : View.GONE);

                handler.postDelayed(() -> {
                    mRenoirSwitchBar.setProgressBarVisible(false);
                    mRenoirSwitchBar.setEnabled(true);
                    setLayoutEnabled(renoirCcSwitchLayout, !mRenoirLsWallpaper);
                    setLayoutEnabled(renoirLsSwitchLayout, !isChecked);
                }, 1500);
            }
        });

        initColorPicker();
    }

    private void initColorPicker() {
        GradientDrawable circleDrawable = (GradientDrawable) ((RippleDrawable) renoirCcPickerView.getForeground()).getDrawable(0);

        circleDrawable.setColor(mRenoirCustomColor);
        float[] startColor = new float[3];
        Color.colorToHSV(mRenoirCustomColor, startColor);

        ColorPickerDialog mColorPickerDialog = new ColorPickerDialog(mContext, 2, startColor);
        mColorPickerDialog.setColorPickerChangeListener(new ColorPickerDialog.ColorPickerChangedListener() {
            @Override
            public void onColorChanged(int i, float[] fArr) {
                mRenoirSwitchBar.setProgressBarVisible(true);
                mRenoirSwitchBar.setEnabled(false);

                mRenoirCustomColor = Color.HSVToColor(fArr);
                circleDrawable.setColor(ColorStateList.valueOf(mRenoirCustomColor));
                RenoirService.setColorBasedOnCustom(mContext, mRenoirCcSwitch.isChecked(), mRenoirCustomColor);

                handler.postDelayed(() -> {
                    mRenoirSwitchBar.setProgressBarVisible(false);
                    mRenoirSwitchBar.setEnabled(true);
                }, 1500);
            }

            @Override
            public void onViewModeChanged(int i) {

            }
        });

        renoirCcPickerView.setOnClickListener(v -> mColorPickerDialog.show());
    }

}
