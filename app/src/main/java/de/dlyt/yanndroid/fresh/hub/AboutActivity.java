package de.dlyt.yanndroid.fresh.hub;

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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.fresh.database.TnsOta;
import de.dlyt.yanndroid.fresh.hub.utils.UpdateApp;
import de.dlyt.yanndroid.samsung.layout.ToolbarLayout;

public class AboutActivity extends AppCompatActivity {

    public static Context mContext;

    public static void reEnableUpdateButton(Context context) {
        MaterialButton updateButton = ((Activity) context).findViewById(R.id.update_button);
        updateButton.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setExpanded(false, false);
        setSupportActionBar(toolbarLayout.getToolbar());
        toolbarLayout.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView app_version = findViewById(R.id.version);

        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String string_version = getString(R.string.about_version_title) + " " + packageInfo.versionName;
            app_version.setText(string_version);
        } catch (PackageManager.NameNotFoundException e) {
            app_version.setText(" ");
        }

        Boolean updateAvailable = false;

        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            int appServerVersion = TnsOta.getAppVersion(mContext);
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
                UpdateApp.DownloadAndInstall(mContext, TnsOta.getAppUrl(mContext));
                updateButton.setEnabled(false);
            }
        });


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

    public void openOpenSource(View v) {
        startActivity(new Intent().setClass(getApplicationContext(), OpenSourceActivity.class));
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