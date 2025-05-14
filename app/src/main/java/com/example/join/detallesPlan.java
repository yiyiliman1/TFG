package com.example.join;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class detallesPlan extends AppCompatActivity {

    TextView nombreTxt, categoriaTxt, distanciaTxt, descripcionTxt, direccionTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_plan); // Asegurate de que el layout se llame así o cambialo

        nombreTxt = findViewById(R.id.textView15);
        categoriaTxt = findViewById(R.id.textView16);
        distanciaTxt = findViewById(R.id.textView17);
        descripcionTxt = findViewById(R.id.textView27);
        direccionTxt = findViewById(R.id.textView19); // si esta etiqueta muestra la dirección

        Intent intent = getIntent();
        if (intent != null) {
            String nombre = intent.getStringExtra("nombre");
            String categoria = intent.getStringExtra("categoria");
            String distancia = intent.getStringExtra("distancia");
            String descripcion = intent.getStringExtra("descripcion");
            String direccion = intent.getStringExtra("direccion");

            nombreTxt.setText(nombre);
            categoriaTxt.setText(categoria);
            distanciaTxt.setText(distancia);
            descripcionTxt.setText(descripcion);
            direccionTxt.setText("📍 " + direccion);
        }
    }
}
