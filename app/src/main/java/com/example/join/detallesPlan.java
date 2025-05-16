package com.example.join;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class detallesPlan extends AppCompatActivity {

    TextView nombreTxt, categoriaTxt, distanciaTxt, descripcionTxt, direccionTxt;
    Button botonUnirse, botonSalir;

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
        botonSalir = findViewById(R.id.botonSalir);

        Intent intent = getIntent();
        if (intent != null) {
            String nombre = intent.getStringExtra("nombre");
            String categoria = intent.getStringExtra("categoria");
            String distancia = intent.getStringExtra("distancia");
            String descripcion = intent.getStringExtra("descripcion");
            String direccion = intent.getStringExtra("direccion");
            String planId = intent.getStringExtra("planId");

            nombreTxt.setText(nombre);
            categoriaTxt.setText(categoria);
            distanciaTxt.setText(distancia);
            descripcionTxt.setText(descripcion);
            direccionTxt.setText("📍 " + direccion);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (planId == null || planId.isEmpty() || currentUser == null) {
                Toast.makeText(this, "Error: planId o usuario no válido", Toast.LENGTH_LONG).show();
                return;
            }

            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference planRef = db.collection("planes").document(planId);

            // Verificar si el usuario ya está unido
            planRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> participantes = (List<String>) documentSnapshot.get("participantes");
                    if (participantes == null) participantes = new ArrayList<>();

                    if (participantes.contains(userId)) {
                        botonUnirse.setEnabled(false);
                        botonSalir.setVisibility(View.VISIBLE);
                    } else {
                        botonUnirse.setEnabled(true);
                        botonSalir.setVisibility(View.GONE);
                    }
                }
            });

            botonUnirse.setOnClickListener(v -> {
                planRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> participantes = (List<String>) documentSnapshot.get("participantes");
                        Long limite = documentSnapshot.getLong("limiteParticipantes");

                        if (participantes == null) participantes = new ArrayList<>();

                        if (participantes.contains(userId)) {
                            Toast.makeText(this, "Ya estás unido a este plan", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (limite != null && limite != -1 && participantes.size() >= limite) {
                            Toast.makeText(this, "El plan está lleno", Toast.LENGTH_SHORT).show();
                            return;
                        }


                        planRef.update("participantes", FieldValue.arrayUnion(userId))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Te has unido al plan", Toast.LENGTH_SHORT).show();
                                    botonUnirse.setEnabled(false);
                                    botonSalir.setVisibility(View.VISIBLE);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error al unirse al plan", Toast.LENGTH_SHORT).show()
                                );
                    }
                });
            });

            botonSalir.setOnClickListener(v -> {
                planRef.update("participantes", FieldValue.arrayRemove(userId))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Has salido del plan", Toast.LENGTH_SHORT).show();
                            botonUnirse.setEnabled(true);
                            botonSalir.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error al salir del plan", Toast.LENGTH_SHORT).show()
                        );
            });
        }
    }
}
