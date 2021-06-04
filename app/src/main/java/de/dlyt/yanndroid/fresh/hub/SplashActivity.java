package de.dlyt.yanndroid.fresh.hub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;

import androidx.appcompat.app.AppCompatActivity;

import de.dlyt.yanndroid.fresh.R;
import de.dlyt.yanndroid.samsung.layout.SplashViewAnimated;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SplashViewAnimated splash_anim = findViewById(R.id.splash_view);
        splash_anim.setSplashAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent().setClass(getApplicationContext(), MainActivity.class));
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            splash_anim.startSplashAnimation();
        }, 500);
    }
}