package de.dlyt.yanndroid.freshapp.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;

import de.dlyt.yanndroid.freshapp.R;
import de.dlyt.yanndroid.freshapp.utils.RomUpdate;
import de.dlyt.yanndroid.freshapp.utils.UpdateApp;

public class AboutActivity extends AppCompatActivity {

    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.ota_about);

        initToolbar();

        TextView app_version = findViewById(R.id.version);

        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String string_version = getString(R.string.about_version_title) + " " + packageInfo.versionName;
            app_version.setText(string_version);
        } catch (PackageManager.NameNotFoundException e) {
            app_version.setText(" ");
        }

        TextView expanded_subtitle = findViewById(R.id.expanded_subtitle);
        settilte("");
        expanded_subtitle.setText("");

        Boolean updateAvailable = false;

        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            int appServerVersion = RomUpdate.getAppVersion(mContext);
            int appLocalVersion = packageInfo.versionCode;
            if (appLocalVersion < appServerVersion) updateAvailable = true;
        } catch (PackageManager.NameNotFoundException e) {
            updateAvailable = false;
        }

        TextView statusText = findViewById(R.id.status_text);
        ProgressBar loadingBar = findViewById(R.id.loading_bar);
        MaterialButton updateButton = findViewById(R.id.update_button);

        loadingBar.setVisibility(View.GONE);

        statusText.setText(updateAvailable ? getResources().getString(R.string.a_new_version_is_available) : getResources().getString(R.string.the_latest_version_is_already_installed));
        updateButton.setVisibility(updateAvailable ? View.VISIBLE : View.GONE);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getPackageManager().canRequestPackageInstalls()) {
                    startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getApplicationContext().getPackageName())));
                }
                UpdateApp.DownloadAndInstall(mContext, RomUpdate.getAppUrl(mContext));
                updateButton.setEnabled(false);
            }
        });


    }

    public static void reEnableUpdateButton(Context context) {
        MaterialButton updateButton = ((Activity) context).findViewById(R.id.update_button);
        updateButton.setEnabled(true);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.app_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ai_settings) {
            callAppInfo();
        }
        return super.onOptionsItemSelected(item);
    }

    private void callAppInfo() {
        try {
            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivity(intent);
        } catch (ActivityNotFoundException unused) {
            startActivity(new Intent("android.settings.MANAGE_APPLICATIONS_SETTINGS"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}