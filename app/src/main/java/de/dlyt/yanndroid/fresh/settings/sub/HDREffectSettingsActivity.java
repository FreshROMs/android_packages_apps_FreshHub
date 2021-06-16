package de.dlyt.yanndroid.fresh.settings.sub;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.samsung.SwitchBar;
import de.dlyt.yanndroid.samsung.layout.ToolbarLayout;
import io.tensevntysevn.fresh.ExperienceUtils;

public class HDREffectSettingsActivity extends AppCompatActivity {

    Context mContext = this;
    public static Handler UIHandler;
    private static int mAppListCount;

    public static void setLayoutEnabled(View view, boolean enable) {
        view.setEnabled(enable);
        view.setClickable(enable);
        view.setFocusable(enable);
        view.setAlpha(enable ? 1f : 0.7f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zest_hdr_effect_settings);

        UIHandler = new Handler(Looper.getMainLooper());
        mContext = this;

        ToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        toolbarLayout.setExpanded(false, false);
        setSupportActionBar(toolbarLayout.getToolbar());
        toolbarLayout.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        SwitchBar mHDReffectSwitch = findViewById(R.id.zest_switch_hdr_effect);
        boolean mHDReffectEnabled = ExperienceUtils.isVideoEnhancerEnabled(mContext);

        mHDReffectSwitch.addOnSwitchChangeListener(new SwitchBar.OnSwitchChangeListener() {
            @Override
            public void onSwitchChanged(SwitchCompat switchCompat, boolean bool) {
                ExperienceUtils.setVideoEnhancerEnabled(mContext, bool);
                updatePreviewImg(bool);
            }
        });

        mHDReffectSwitch.setChecked(mHDReffectEnabled);
        populateAppList(mContext);
    }

    private void updatePreviewImg(Boolean bool) {
        ImageView mHDReffectPreview = findViewById(R.id.zest_hdr_effect_preview_img);
        Drawable previewDisabled = AppCompatResources.getDrawable(mContext, R.drawable.hdr_effect_preview_off);
        Drawable previewEnabled = AppCompatResources.getDrawable(mContext, R.drawable.hdr_effect_preview_on);
        mHDReffectPreview.setImageDrawable(bool ? previewEnabled : previewDisabled);
    }

    public static void runOnUI(Runnable runnable) {
        UIHandler.postDelayed(runnable, 2000);
    }

    private void populateAppList(Context context) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String[] mCompatibleList = getResources().getStringArray(R.array.hdr_effect_app_compatible_list);
        List<ApplicationInfo> mAppArray = new ArrayList<>();

        LinearLayout semProgress = findViewById(R.id.zest_hdr_effect_listview_progress);
        ListView mListview = findViewById(R.id.zest_hdr_effect_listview);

        mListview.setVisibility(View.GONE);
        semProgress.setVisibility(View.VISIBLE);

        executor.execute(() -> {
            for (String s : mCompatibleList) {
                try {
                    PackageManager pm = context.getPackageManager();
                    android.content.pm.ApplicationInfo info = pm.getApplicationInfo(s, 0);
                    Drawable icon = pm.getApplicationIcon(s);
                    String name = pm.getApplicationLabel(info).toString();

                    mAppArray.add(new ApplicationInfo(name, icon));
                } catch (PackageManager.NameNotFoundException ignored) {
                    // App not found
                }
            }

            runOnUI(() -> {
                mAppListCount = mAppArray.toArray().length;
                int listHeight = (int) (mAppListCount * 58);
                if (listHeight > 300) listHeight = 300;
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, listHeight, context.getResources().getDisplayMetrics());

                semProgress.setVisibility(View.GONE);
                mListview.setVisibility(View.INVISIBLE);
                mListview.getLayoutParams().height = dimensionInDp;
                mListview.requestLayout();
                mListview.setVisibility(View.VISIBLE);
                mListview.setAdapter(new AppListAdapter(this, mAppArray));
            });
        });
    }

    public static class AppListAdapter extends ArrayAdapter<ApplicationInfo> {
        List<ApplicationInfo> mAppList;

        public AppListAdapter(Context context, List<ApplicationInfo> appList) {
            super(context, R.layout.card_application_list_item, appList);
            this.mAppList = appList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ApplicationInfo item = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.card_application_list_item, parent, false);
            }

            TextView appName = convertView.findViewById(R.id.hdr_effect_app_name);
            ImageView appIcon = convertView.findViewById(R.id.hdr_effect_app_icon);
            LinearLayout appDivider = convertView.findViewById(R.id.hdr_effect_divider);

            assert item != null;
            appName.setText(item.mAppName);
            appIcon.setImageDrawable(item.mAppIcon);

            if (position == 0) {
                appDivider.setVisibility(View.GONE);
            }

            return convertView;
        }
    }

    public static class ApplicationInfo {
        public String mAppName;
        public Drawable mAppIcon;

        public ApplicationInfo(String appName, Drawable appIcon) {
            mAppName = appName;
            mAppIcon = appIcon;
        }
    }
}