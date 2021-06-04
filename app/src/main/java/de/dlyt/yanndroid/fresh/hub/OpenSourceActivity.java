package de.dlyt.yanndroid.fresh.hub;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.samsung.layout.ToolbarLayout;

public class OpenSourceActivity extends AppCompatActivity {

    public static Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_source);

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setExpanded(false, false);
        setSupportActionBar(toolbarLayout.getToolbar());
        toolbarLayout.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    public void openOtaUpdates(View v) {
        String url = "https://github.com/MatthewBooth/OTAUpdates";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openBlissOta(View v) {
        String url = "https://github.com/chummydevteam/TeamBlissOTA";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openBypass(View v) {
        String url = "https://github.com/Uncodin/bypass";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openAndroidX(View v) {
        String url = "https://android.googlesource.com/platform/frameworks/support";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openRootTools(View v) {
        String url = "https://github.com/Stericson/RootTools";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openImageLoader(View v) {
        String url = "https://github.com/nostra13/Android-Universal-Image-Loader";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openSecondScreen(View v) {
        String url = "https://github.com/farmerbb/SecondScreen";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openResChanger(View v) {
        String url = "https://github.com/tytydraco/Resolution-Changer";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void openOneUI(View v) {
        String url = "https://github.com/Yanndroid/SamsungOneUi";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}