package de.dlyt.yanndroid.fresh.hub;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Animation splash_anim;
                splash_anim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.splash_animation);

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

                fresh_fg.startAnimation(splash_anim);

                /*fresh_fg.animate().setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startActivity(new Intent().setClass(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                }).rotation(360).setDuration(1000).start();*/
            }
        }, getResources().getInteger(R.integer.splash_time));

    }
}