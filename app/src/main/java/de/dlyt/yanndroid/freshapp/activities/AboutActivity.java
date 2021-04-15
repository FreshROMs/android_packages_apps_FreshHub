package de.dlyt.yanndroid.freshapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;

import de.dlyt.yanndroid.freshapp.R;

public class AboutActivity extends AppCompatActivity {

    public static Context mContext;

    public void initToolbar() {
        /** Def */
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.ota_about);

        TextView app_version = findViewById(R.id.version);

        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String string_version = getString(R.string.about_version_title) + " " + packageInfo.versionName;
            app_version.setText(string_version);
        } catch (PackageManager.NameNotFoundException e) {
            app_version.setText(" ");
        }

        TextView expanded_subtitle = findViewById(R.id.expanded_subtitle);

        initToolbar();
        settilte(" ");
        expanded_subtitle.setText(" ");
    }
}