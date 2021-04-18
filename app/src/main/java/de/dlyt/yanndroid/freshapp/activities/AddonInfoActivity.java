package de.dlyt.yanndroid.freshapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.AppBarLayout;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.dlyt.yanndroid.freshapp.R;
import in.uncod.android.bypass.Bypass;

public class AddonInfoActivity extends AppCompatActivity {

    public static Context mContext;
    public static ImageLoader mImageLoader;
    public static TextView mDownloadedSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addon_info);
        mImageLoader = ImageLoader.getInstance();

        initToolbar();

        TextView expanded_subtitle = findViewById(R.id.expanded_subtitle);
        expanded_subtitle.setText("");

        final ImageView addonThumbnail = (ImageView) findViewById(R.id.addon_info_thumbnail);
        final TextView addonVersion = (TextView) findViewById(R.id.version_number);
        final TextView addonPackageName = (TextView) findViewById(R.id.package_name);
        final TextView addonFullInfo = (TextView) findViewById(R.id.addon_information_description);
        final TextView addonTotalSize = (TextView) findViewById(R.id.addon_download_total);
        final TextView addonName = (TextView) findViewById(R.id.title);
        mDownloadedSize = (TextView) findViewById(R.id.addon_download_size);

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        String name = intent.getStringExtra("name");
        String packageName = intent.getStringExtra("packageName");
        String downloadUrl = intent.getStringExtra("downloadUrl");
        String totalSize = intent.getStringExtra("totalSize");
        String versionName = intent.getStringExtra("versionName");
        String fullInfo = intent.getStringExtra("fullInfo");
        String versionNumber = intent.getStringExtra("versionNumber");
        String thumbnailUrl = intent.getStringExtra("thumbnailUrl");

        mImageLoader.displayImage(thumbnailUrl, addonThumbnail);
        addonVersion.setText(versionName);
        addonPackageName.setText(packageName);
        addonTotalSize.setText(totalSize);
        addonName.setText(name);
        settilte("");

        TextView collapsed_title = findViewById(R.id.collapsed_title);
        collapsed_title.setText(name);
        collapsed_title.setAlpha(0);

        ScrollView content_scroll = findViewById(R.id.content_scroll);
        content_scroll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                collapsed_title.setAlpha((float) (scrollY) / 120);
            }
        });


        Bypass byPass = new Bypass(mContext);
        CharSequence string = byPass.markdownToSpannable(fullInfo);
        addonFullInfo.setText(string);
        addonFullInfo.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void initToolbar() {
        /** Def */
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppBarLayout AppBar = findViewById(R.id.app_bar);


        /** 1/3 of the Screen */
        ViewGroup.LayoutParams layoutParams = AppBar.getLayoutParams();
        layoutParams.height = (int) ((double) this.getResources().getDisplayMetrics().heightPixels / 2.6);

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
}