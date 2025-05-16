package com.example.join;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class detallesPlan extends AppCompatActivity {

    TextView nombreTxt, categoriaTxt, distanciaTxt, descripcionTxt, direccionTxt;
    Button botonUnirse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_plan);

        nombreTxt = findViewById(R.id.textView15);
        categoriaTxt = findViewById(R.id.textView16);
        distanciaTxt = findViewById(R.id.textView17);
        descripcionTxt = findViewById(R.id.textView27);
        direccionTxt = findViewById(R.id.textView19);
        botonUnirse = findViewById(R.id.botonUnirse);

        Intent intent = getIntent();
        if (intent != null) {
            String nombre = intent.getStringExtra("nombre");
            String categoria = intent.getStringExtra("categoria");
            String distancia = intent.getStringExtra("distancia");
            String descripcion = intent.getStringExtra("descripcion");
            String direccion = intent.getStringExtra("direccion");
            String planId = getIntent().getStringExtra("planId");


            nombreTxt.setText(nombre);
            categoriaTxt.setText(categoria);
            distanciaTxt.setText(distancia);
            descripcionTxt.setText(descripcion);
            direccionTxt.setText("📍 " + direccion);

            botonUnirse.setOnClickListener(v -> {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference planRef = db.collection("planes").document(planId);

                planRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> participantes = (List<String>) documentSnapshot.get("participantes");
                        Long limite = documentSnapshot.getLong("limiteParticipantes");

                        if (participantes == null) participantes = new ArrayList<>();

                        if (participantes.contains(userId)) {
                            Toast.makeText(this, "Ya estás unido a este plan", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (limite != null && participantes.size() >= limite) {
                            Toast.makeText(this, "El plan está lleno", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        planRef.update("participantes", FieldValue.arrayUnion(userId))
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(this, "Te has unido al plan", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error al unirse al plan", Toast.LENGTH_SHORT).show()
                                );
                    } else {
                        Toast.makeText(this, "Plan no encontrado", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }
}
