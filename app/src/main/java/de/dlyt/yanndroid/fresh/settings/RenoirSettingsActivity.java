package de.dlyt.yanndroid.fresh.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SeslSwitchBar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.switchmaterial.SwitchMaterial;

import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.hub.utils.Preferences;
import de.dlyt.yanndroid.fresh.renoir.RenoirService;
import de.dlyt.yanndroid.fresh.utils.JobScheduler;

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

        SeslSwitchBar renoirSwitch = findViewById(R.id.switch_renoir_enabled);
        TextView renoirDescription = findViewById(R.id.renoir_description_text);
        LinearLayout renoirLsSwitchLayout = findViewById(R.id.switch_renoir_lock_screen_layout);
        SwitchMaterial renoirLsSwitch = findViewById(R.id.switch_renoir_lock_screen);

        initToolbar();
        settilte(getResources().getString(R.string.renoir_settings_title));
        setSubtitle(" ");

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

    public void setSubtitle(String subtitle) {
        TextView expanded_subtitle = findViewById(R.id.expanded_subtitle);
        expanded_subtitle.setText(subtitle);
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
        ImageView navigationIcon = findViewById(R.id.navigationIcon);
        View navigationIcon_Badge = findViewById(R.id.navigationIcon_new_badge);
        navigationIcon_Badge.setVisibility(View.GONE);
        navigationIcon.setImageResource(R.drawable.ic_back);
        navigationIcon.setOnClickListener(new View.OnClickListener() {
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

    public void toggleRenoirSwitch(View v) {
        SwitchMaterial renoirSwitch = findViewById(R.id.switch_renoir_lock_screen);
        renoirSwitch.toggle();
    }
}