package com.example.m_learning_onlinecources;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    private ImageView logoImageView;
    private static int SPLASH_TIME_OUT = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        logoImageView = findViewById(R.id.logoImageView);
        TextView appNameTextView = findViewById(R.id.appNameTextView);
        TextView versionTextView = findViewById(R.id.versionTextView);

        // Logo Animation - Scale and Rotate
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoImageView, "scaleX", 0.5f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoImageView, "scaleY", 0.5f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(logoImageView, "rotation", 0f, 360f);
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);
        rotation.setDuration(1000);
        rotation.setInterpolator(new AccelerateDecelerateInterpolator());

        // App Name Text Animation - Fade In
        ObjectAnimator fadeInAppName = ObjectAnimator.ofFloat(appNameTextView, "alpha", 0f, 1f);
        fadeInAppName.setDuration(800);

        // Version Text Animation - Slide Up
        ObjectAnimator slideUpVersion = ObjectAnimator.ofFloat(versionTextView, "translationY", 50f, 0f);
        ObjectAnimator fadeInVersion = ObjectAnimator.ofFloat(versionTextView, "alpha", 0f, 1f);
        slideUpVersion.setDuration(800);
        fadeInVersion.setDuration(800);

        // Combine Animations
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, rotation); // Play logo animations together
        animatorSet.play(fadeInAppName).after(rotation); // Play app name animation after logo animation
        animatorSet.playTogether(slideUpVersion, fadeInVersion); // Play version animations together
        animatorSet.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, LoginScreen.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}