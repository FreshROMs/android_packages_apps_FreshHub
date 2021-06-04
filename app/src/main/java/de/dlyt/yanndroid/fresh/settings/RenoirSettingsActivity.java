package de.dlyt.yanndroid.fresh.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.renoir.RenoirService;
import de.dlyt.yanndroid.samsung.SwitchBar;
import de.dlyt.yanndroid.samsung.layout.ToolbarLayout;

public class RenoirSettingsActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub_renoir_settings);

        mContext = this;
        Handler handler = new Handler(Looper.getMainLooper());

        Boolean mRenoirEnabled = RenoirService.getRenoirEnabled(mContext);
        Boolean mRenoirLsWallpaper = RenoirService.getColorBasedOnLock(mContext);

        SwitchBar renoirSwitch = findViewById(R.id.switch_renoir_enabled);
        TextView renoirDescription = findViewById(R.id.renoir_description_text);
        LinearLayout renoirLsSwitchLayout = findViewById(R.id.switch_renoir_lock_screen_layout);
        SwitchMaterial renoirLsSwitch = findViewById(R.id.switch_renoir_lock_screen);

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        setSupportActionBar(toolbarLayout.getToolbar());
        toolbarLayout.setExpanded(false, false);
        toolbarLayout.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        if (RenoirService.isGalaxyThemeApplied(mContext)) {
            renoirLsSwitch.setEnabled(false);
            setLayoutEnabled(renoirLsSwitchLayout, false);
            renoirDescription.setText(getString(R.string.renoir_settings_desc_unavailable));
        } else {
            setLayoutEnabled(renoirLsSwitchLayout, mRenoirEnabled);
            renoirLsSwitch.setEnabled(mRenoirLsWallpaper);
        }

        renoirSwitch.addOnSwitchChangeListener((buttonView, isChecked) -> {
            renoirSwitch.setProgressBarVisible(true);
            renoirSwitch.setEnabled(false);
            RenoirService.setRenoirEnabled(mContext, isChecked);

            if (!isChecked) {
                renoirLsSwitch.setEnabled(false);
                setLayoutEnabled(renoirLsSwitchLayout, false);
            }

            handler.postDelayed(() -> {
                renoirSwitch.setProgressBarVisible(false);
                renoirSwitch.setEnabled(true);
                if (isChecked) setLayoutEnabled(renoirLsSwitchLayout, true);
            }, 1500);
        });

        renoirLsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            renoirSwitch.setProgressBarVisible(true);
            renoirSwitch.setEnabled(false);
            setLayoutEnabled(renoirLsSwitchLayout, false);
            RenoirService.setColorBasedOnLock(mContext, isChecked);

            handler.postDelayed(() -> {
                renoirSwitch.setProgressBarVisible(false);
                renoirSwitch.setEnabled(true);
                setLayoutEnabled(renoirLsSwitchLayout, true);
            }, 1500);
        });
    }

    public static void setLayoutEnabled(LinearLayout view, boolean enable) {
        view.setEnabled(enable);
        view.setClickable(enable);
        view.setFocusable(enable);
        view.setAlpha(enable ? 1f : 0.7f);
    }

    public void toggleRenoirSwitch(View v) {
        SwitchMaterial renoirSwitch = findViewById(R.id.switch_renoir_lock_screen);
        renoirSwitch.toggle();
    }
}