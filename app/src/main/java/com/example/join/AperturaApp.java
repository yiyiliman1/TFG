package com.example.join;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AperturaApp extends AppCompatActivity {

    private static final int SPLASH_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apertura_app);

        ImageView iconApp = findViewById(R.id.iconjoin);
        ImageView iconName = findViewById(R.id.iconName);

        AlphaAnimation fadeInApp = new AlphaAnimation(0, 1);
        fadeInApp.setDuration(1500);
        iconApp.setVisibility(View.VISIBLE);
        iconApp.startAnimation(fadeInApp);

        AlphaAnimation fadeInName = new AlphaAnimation(0, 1);
        fadeInName.setDuration(1500);
        iconName.setVisibility(View.VISIBLE);
        iconName.startAnimation(fadeInName);


        new Handler().postDelayed(() -> {
            Intent intent = new Intent(AperturaApp.this, login.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION);
    }
}