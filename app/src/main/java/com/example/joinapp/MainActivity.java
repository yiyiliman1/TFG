package com.example.joinapp;

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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageView iconapp = findViewById(R.id.iconjoin);
        ImageView iconname = findViewById(R.id.IconName);

        // Listener para cambiar de actividad
        iconname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InicioSesion.class);
                startActivity(intent);
            }
        });

        // Animación de fade-in para el icono principal
        AlphaAnimation fadeInIconApp = new AlphaAnimation(0, 1);
        fadeInIconApp.setDuration(2000);
        iconapp.setVisibility(View.VISIBLE);
        iconapp.startAnimation(fadeInIconApp);

        // Espera 3 segundos para mostrar el segundo icono
        new Handler().postDelayed(() -> {
            AlphaAnimation fadeInIconName = new AlphaAnimation(0, 1);
            fadeInIconName.setDuration(2000);
            iconname.setVisibility(View.VISIBLE);
            iconname.startAnimation(fadeInIconName);

            // Espera otros 2 segundos y hace animación de rebote
            new Handler().postDelayed(() -> {
                ScaleAnimation scaleAnimation = new ScaleAnimation(
                        1f, 1.2f,
                        1f, 1.2f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                );
                scaleAnimation.setDuration(600);
                scaleAnimation.setRepeatCount(Animation.INFINITE);
                scaleAnimation.setRepeatMode(Animation.REVERSE);

                iconname.startAnimation(scaleAnimation);
            }, 2000);

        }, 3000);
    }
}
