package com.example.join;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class detallesPlan extends AppCompatActivity {

    TextView nombreTxt, categoriaTxt, distanciaTxt, descripcionTxt, direccionTxt, esTuyoTxt;
    Button botonUnirse, botonSalir, botonVerUsuarios, botonChat, botonCancelarPlan;
    LinearLayout layoutUsuarios;

    private String planId, nombre;

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
        esTuyoTxt = findViewById(R.id.textViewEsTuyo);
        botonVerUsuarios = findViewById(R.id.botonVerUsuarios);
        layoutUsuarios = findViewById(R.id.layoutUsuariosUnidos);
        botonChat = findViewById(R.id.botonAbrirChat);
        botonCancelarPlan = findViewById(R.id.botonCancelarPlan);

        Intent intent = getIntent();
        if (intent != null) {
            nombre = intent.getStringExtra("nombre");
            String categoria = intent.getStringExtra("categoria");
            String distancia = intent.getStringExtra("distancia");
            String descripcion = intent.getStringExtra("descripcion");
            String direccion = intent.getStringExtra("direccion");
            planId = intent.getStringExtra("planId");

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

            planRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String creadorId = documentSnapshot.getString("creadorId");
                    if (creadorId != null && creadorId.equals(userId)) {
                        esTuyoTxt.setVisibility(View.VISIBLE);
                        botonUnirse.setVisibility(View.GONE);
                        botonSalir.setVisibility(View.GONE);
                        botonCancelarPlan.setVisibility(View.VISIBLE);
                    }

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

            botonCancelarPlan.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("¿Cancelar plan?")
                        .setMessage("Esta acción no se puede deshacer. ¿Deseas cancelar este plan?")
                        .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                            Map<String, Object> actualizaciones = new HashMap<>();
                            actualizaciones.put("estado", "cancelado");
                            actualizaciones.put("participantes", new ArrayList<String>());

                            planRef.update(actualizaciones)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Plan cancelado y participantes eliminados", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Error al cancelar el plan", Toast.LENGTH_SHORT).show()
                                    );
                        })
                        .setNegativeButton("No", null)
                        .show();
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

            botonVerUsuarios.setOnClickListener(v -> {
                if (layoutUsuarios.getVisibility() == View.VISIBLE) {
                    layoutUsuarios.setVisibility(View.GONE);
                    botonVerUsuarios.setText("Ver usuarios unidos");
                } else {
                    layoutUsuarios.removeAllViews();
                    layoutUsuarios.setVisibility(View.VISIBLE);
                    botonVerUsuarios.setText("Ocultar usuarios");

                    planRef.get().addOnSuccessListener(docSnap -> {
                        if (docSnap.exists()) {
                            List<String> participantes = (List<String>) docSnap.get("participantes");
                            if (participantes == null || participantes.isEmpty()) {
                                TextView vacio = new TextView(this);
                                vacio.setText("No hay usuarios unidos");
                                layoutUsuarios.addView(vacio);
                                return;
                            }

                            String creadorId = docSnap.getString("creadorId");

                            for (String uid : participantes) {
                                db.collection("usuarios").document(uid).get().addOnSuccessListener(userSnap -> {
                                    if (userSnap.exists()) {
                                        String nombreUser = userSnap.getString("usuario");
                                        String fotoUrl = userSnap.getString("fotoPerfil");

                                        View userView = getLayoutInflater().inflate(R.layout.item_usuario_unido, null);
                                        TextView nombreTxt = userView.findViewById(R.id.nombreUsuario);
                                        ImageView imgUser = userView.findViewById(R.id.imagenUsuario);
                                        Button botonEliminar = userView.findViewById(R.id.botonEliminarUsuario);

                                        nombreTxt.setText(nombreUser != null ? nombreUser : "Sin nombre");

                                        if (fotoUrl != null && !fotoUrl.isEmpty()) {
                                            Glide.with(this).load(fotoUrl).into(imgUser);
                                        } else {
                                            imgUser.setImageResource(R.drawable.default_user);
                                        }

                                        View.OnClickListener perfilListener = view -> {
                                            Intent intentPerfil = new Intent(this, PerfilUsuario.class);
                                            intentPerfil.putExtra("usuarioId", uid);
                                            startActivity(intentPerfil);
                                        };
                                        nombreTxt.setOnClickListener(perfilListener);
                                        imgUser.setOnClickListener(perfilListener);

                                        if (creadorId != null && creadorId.equals(userId) && !uid.equals(userId)) {
                                            botonEliminar.setVisibility(View.VISIBLE);
                                            botonEliminar.setOnClickListener(view -> {
                                                new android.app.AlertDialog.Builder(this)
                                                        .setTitle("Confirmar eliminación")
                                                        .setMessage("¿Seguro que deseas eliminar a este usuario del plan?")
                                                        .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                                                            planRef.update("participantes", FieldValue.arrayRemove(uid))
                                                                    .addOnSuccessListener(aVoid -> {
                                                                        Toast.makeText(this, "Usuario eliminado del plan", Toast.LENGTH_SHORT).show();
                                                                        layoutUsuarios.removeView(userView);
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Toast.makeText(this, "Error al eliminar usuario", Toast.LENGTH_SHORT).show();
                                                                    });
                                                        })
                                                        .setNegativeButton("Cancelar", null)
                                                        .show();
                                            });
                                        } else {
                                            botonEliminar.setVisibility(View.GONE);
                                        }

                                        layoutUsuarios.addView(userView);
                                    }
                                });
                            }
                        }
                    });
                }
            });

            botonChat.setOnClickListener(view -> {
                if (planId != null && nombre != null) {
                    Intent intentChat = new Intent(this, ChatPlan.class);
                    intentChat.putExtra("planId", planId);
                    intentChat.putExtra("nombre", nombre);
                    startActivity(intentChat);
                } else {
                    Toast.makeText(this, "No se pudo abrir el chat: información incompleta", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
