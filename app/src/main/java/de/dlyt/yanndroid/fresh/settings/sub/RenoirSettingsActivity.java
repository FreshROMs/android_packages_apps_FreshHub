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

import com.google.android.material.card.MaterialCardView;
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

        // colorPickerDemo();
    }

    /*
    private void colorPickerDemo() {
        MaterialCardView custom_color_card = findViewById(R.id.custom_color_card);
        View custom_color_circle = findViewById(R.id.custom_color_circle);
        GradientDrawable circleDrawable = (GradientDrawable) ((RippleDrawable) custom_color_circle.getBackground()).getDrawable(0);

        custom_color_card.setOnClickListener(v -> {

            float[] startColor = new float[3];
            Color.colorToHSV(getResources().getColor(R.color.default_primary_color), startColor);

            ColorPickerDialog mColorPickerDialog;
            mColorPickerDialog = new ColorPickerDialog(mContext, 2, startColor);
            mColorPickerDialog.setColorPickerChangeListener(new ColorPickerDialog.ColorPickerChangedListener() {
                @Override
                public void onColorChanged(int i, float[] fArr) {
                    circleDrawable.setColor(ColorStateList.valueOf(Color.HSVToColor(fArr)));
                }

                @Override
                public void onViewModeChanged(int i) {

                }
            });
            mColorPickerDialog.show();

        });
    } */


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
        TextView renoirDescription = findViewById(R.id.renoir_description_text);
        
        if (ExperienceUtils.isGalaxyThemeApplied(mContext)) {
            mRenoirLsSwitch.setEnabled(false);
            setLayoutEnabled(renoirLsSwitchLayout, false);
            renoirDescription.setText(getString(R.string.renoir_settings_desc_unavailable));
            mRenoirEnabled = false;
            mRenoirLsWallpaper = false;
        } else if (ExperienceUtils.isLsWallpaperUnavailable(mContext)) {
            setLayoutEnabled(renoirLsSwitchLayout, false);
            mRenoirLsSwitch.setEnabled(false);
            mRenoirLsWallpaper = false;
        } else {
            setLayoutEnabled(renoirLsSwitchLayout, mRenoirEnabled);
            mRenoirLsSwitch.setEnabled(mRenoirLsWallpaper);
            mRenoirEnabled = RenoirService.getRenoirEnabled(mContext);
            mRenoirLsWallpaper = RenoirService.getColorBasedOnLock(mContext);
        }

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
