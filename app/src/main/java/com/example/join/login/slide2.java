package com.example.join.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.join.R;

public class slide2 extends AppCompatActivity {

    private ProgressBar storyProgress;
    private ImageView storyImage;
    private TextView storyTitle, storyDescription;

    private final int SLIDE_DURATION_MS = 3000; // duración total de la barra en ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_slide2);

        // Insets para modo EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        storyProgress = findViewById(R.id.storyProgress);
        storyImage = findViewById(R.id.storyImage);
        storyTitle = findViewById(R.id.storyTitle);
        storyDescription = findViewById(R.id.storyDescription);

        // Iniciar animación y transición
        animateProgressBarAndGoToNext();
    }

    private void animateProgressBarAndGoToNext() {
        storyProgress.setProgress(0);
        storyProgress.setMax(100);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            int progress = 0;

            @Override
            public void run() {
                progress += 5;
                storyProgress.setProgress(progress);

                if (progress < 100) {
                    handler.postDelayed(this, SLIDE_DURATION_MS / 20);
                } else {
                    // Ir a la siguiente pantalla (ajusta el nombre si es diferente)
                    Intent intent = new Intent(slide2.this, slide3.class);
                    startActivity(intent);
                    finish(); // Cierra esta actividad para que no se pueda volver atrás
                }
            }
        }, 50);
    }
}
