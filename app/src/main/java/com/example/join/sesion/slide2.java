package com.example.join.sesion;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.join.R;

public class slide2 extends AppCompatActivity {

    private ProgressBar progressBar;
    private Handler handler = new Handler();
    private int progressStatus = 0;
    private final int duration = 3000; // 5 segundos
    private final int interval = 50;   // cada 50ms se actualiza el progreso

    private final Runnable progressRunnable = new Runnable() {
        @Override
        public void run() {
            progressStatus += interval;
            progressBar.setProgress(progressStatus);
            if (progressStatus < duration) {
                handler.postDelayed(this, interval);
            } else {
                goToNextSlide();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_slide1);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(duration);
        progressBar.setProgress(0);

        handler.post(progressRunnable);
    }

    private void goToNextSlide() {
        Intent intent = new Intent(slide2.this, slide3.class); // Cambia a tu siguiente slide o pantalla
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(progressRunnable);
    }
}
