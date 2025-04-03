package com.example.joinapp;

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

        ImageView iconname = findViewById(R.id.IconName);
        ImageView iconapp = findViewById(R.id.iconjoin);

        // Crear una animación de desvanecimiento (fade-in) para el primer icono
        AlphaAnimation fadeInIconApp = new AlphaAnimation(0, 1); // De invisible (0) a visible (1)
        fadeInIconApp.setDuration(2000); // Duración de la animación para el primer icono (2 segundos)
        iconapp.setVisibility(View.VISIBLE); // Asegurarse de que el icono sea visible
        iconapp.startAnimation(fadeInIconApp); // Iniciar la animación para el primer icono

        // Usar un Handler para retrasar la aparición del segundo icono
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Crear una animación de desvanecimiento (fade-in) para el segundo icono
                AlphaAnimation fadeInIconName = new AlphaAnimation(0, 1); // De invisible (0) a visible (1)
                fadeInIconName.setDuration(2000); // Duración de la animación para el segundo icono (2 segundos)
                iconname.setVisibility(View.VISIBLE); // Asegurarse de que el icono sea visible
                iconname.startAnimation(fadeInIconName); // Iniciar la animación de desvanecimiento

                // Usar un Handler para retrasar la animación de escala después de que el segundo icono haya aparecido
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Crear una animación de escala para el segundo icono después de que se haya desvanecido
                        ScaleAnimation scaleAnimation = new ScaleAnimation(
                                1f, 1.2f, // Escala de inicio y fin en el eje X
                                1f, 1.2f, // Escala de inicio y fin en el eje Y
                                Animation.RELATIVE_TO_SELF, 0.5f, // Centrado en el eje X
                                Animation.RELATIVE_TO_SELF, 0.5f // Centrado en el eje Y
                        );
                        scaleAnimation.setDuration(600); // Duración de la animación (600 ms)
                        scaleAnimation.setRepeatCount(Animation.INFINITE); // Repetir infinitamente
                        scaleAnimation.setRepeatMode(Animation.REVERSE); // Efecto rebote

                        iconname.startAnimation(scaleAnimation); // Iniciar la animación de escala
                    }
                }, 2000); // Esperar 2 segundos para que termine la animación de desvanecimiento antes de comenzar la animación de escala
            }
        }, 3000); // 3000 milisegundos = 3 segundos de retraso antes de que aparezca el segundo icono
    }
}
