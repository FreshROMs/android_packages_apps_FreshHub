package de.dlyt.yanndroid.fresh.hub;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import de.dlyt.yanndroid.fresh.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView fresh_fg = findViewById(R.id.splash_image_foreground);

        Animation splash_anim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.splash_animation);

        splash_anim.setAnimationListener(new Animation.AnimationListener() {
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fresh_fg.startAnimation(splash_anim);
            }
        }, getResources().getInteger(R.integer.splash_time));

    }
}