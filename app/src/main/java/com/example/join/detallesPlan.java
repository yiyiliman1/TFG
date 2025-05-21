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

import java.text.SimpleDateFormat;
import java.util.*;

public class detallesPlan extends AppCompatActivity {

    TextView nombreTxt, categoriaTxt, distanciaTxt, descripcionTxt, direccionTxt, esTuyoTxt, fechaHoraTxt;
    Button botonUnirse, botonSalir, botonVerUsuarios, botonChat, botonCancelarPlan;
    LinearLayout layoutUsuarios;
    ImageView imageViewPlan;

    private String planId, nombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_plan);

        // Referencias UI
        nombreTxt = findViewById(R.id.textView15);
        categoriaTxt = findViewById(R.id.textView16);
        distanciaTxt = findViewById(R.id.textView17);
        descripcionTxt = findViewById(R.id.textView27);
        direccionTxt = findViewById(R.id.textView19);
        fechaHoraTxt = findViewById(R.id.textViewFechaHora);
        imageViewPlan = findViewById(R.id.imageView16);
        botonUnirse = findViewById(R.id.botonUnirse);
        botonSalir = findViewById(R.id.botonSalir);
        esTuyoTxt = findViewById(R.id.textViewEsTuyo);
        botonVerUsuarios = findViewById(R.id.botonVerUsuarios);
        layoutUsuarios = findViewById(R.id.layoutUsuariosUnidos);
        botonChat = findViewById(R.id.botonAbrirChat);
        botonCancelarPlan = findViewById(R.id.botonCancelarPlan);

        ImageView botonMapa = findViewById(R.id.imageView4);
        botonMapa.setOnClickListener(v -> {
            Intent intent = new Intent(detallesPlan.this, menu.class);
            startActivity(intent);
        });
        ImageView botonPerfil = findViewById(R.id.imageView8);
        botonPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(detallesPlan.this, miPerfil.class);
            startActivity(intent);
        });
        ImageView botonChat = findViewById(R.id.imageView2);
        botonChat.setOnClickListener(v -> {
            Intent intent = new Intent(detallesPlan.this, BuscarUsuario.class);
            startActivity(intent);
        });
        ImageView botonListas = findViewById(R.id.imageView10);
        botonListas.setOnClickListener(v -> {
            Intent intent = new Intent(detallesPlan.this, listarPlanesCercanos.class);
            startActivity(intent);
        });
        ImageView botonPlan = findViewById(R.id.imageView7);
        botonPlan.setOnClickListener(v -> {
            startActivity(new Intent(this, crearNuevoPlan.class));
        });

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

            // Cargar detalles desde Firestore
            planRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String fotoUrl = documentSnapshot.getString("fotoUrl");
                    if (fotoUrl != null && !fotoUrl.isEmpty()) {
                        Glide.with(this).load(fotoUrl).into(imageViewPlan);
                    } else {
                        imageViewPlan.setImageResource(R.drawable.personalogo);
                    }

                    // Fecha y hora
                    com.google.firebase.Timestamp timestamp = documentSnapshot.getTimestamp("fechaHora");
                    if (timestamp != null) {
                        Date fecha = timestamp.toDate();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy - HH:mm", new Locale("es", "ES"));
                        fechaHoraTxt.setText(sdf.format(fecha));
                    }

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

            // Botón cancelar plan
            botonCancelarPlan.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("¿Cancelar plan?")
                        .setMessage("Esta acción no se puede deshacer. ¿Deseas cancelar este plan?")
                        .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                            Map<String, Object> actualizaciones = new HashMap<>();
                            actualizaciones.put("estado", "cancelado");
                            //actualizaciones.put("participantes", new ArrayList<String>());

                            db.collection("planes").document(planId).update(actualizaciones)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Plan cancelado", Toast.LENGTH_SHORT).show();
                                        recreate();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Error al cancelar", Toast.LENGTH_SHORT).show()
                                    );
                        })
                        .setNegativeButton("No", null)
                        .show();
            });

            // Botón unirse
            botonUnirse.setOnClickListener(v -> {
                planRef.get().addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    boolean soloAmigos = Boolean.TRUE.equals(documentSnapshot.getBoolean("soloAmigos"));
                    String creadorId = documentSnapshot.getString("creadorId");

                    if (soloAmigos && creadorId != null && !creadorId.equals(userId)) {
                        // Comprobar si el usuario es amigo del creador
                        db.collection("usuarios").document(creadorId).get().addOnSuccessListener(creadorSnap -> {
                            List<String> amigos = (List<String>) creadorSnap.get("amigos");
                            if (amigos != null && amigos.contains(userId)) {
                                // Es amigo: permitir unirse
                                intentarUnirse(planRef, userId, documentSnapshot);
                            } else {
                                Toast.makeText(this, "Este plan es solo para amigos del creador", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // No es solo para amigos, permitir unirse
                        intentarUnirse(planRef, userId, documentSnapshot);
                    }
                });
            });


            // Botón salir
            botonSalir.setOnClickListener(v -> {
                planRef.update("participantes", FieldValue.arrayRemove(userId))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Has salido del plan", Toast.LENGTH_SHORT).show();
                            botonUnirse.setEnabled(true);
                            botonSalir.setVisibility(View.GONE);
                        });
            });

            // Botón ver usuarios
            botonVerUsuarios.setOnClickListener(v -> {
                if (layoutUsuarios.getVisibility() == View.VISIBLE) {
                    layoutUsuarios.setVisibility(View.GONE);
                    botonVerUsuarios.setText("Ver usuarios unidos");
                } else {
                    layoutUsuarios.setVisibility(View.VISIBLE);
                    botonVerUsuarios.setText("Ocultar usuarios");
                    layoutUsuarios.removeAllViews();

                    planRef.get().addOnSuccessListener(docSnap -> {
                        if (!docSnap.exists()) return;

                        String creadorId = docSnap.getString("creadorId");

                        // Mostrar al creador
                        if (creadorId != null) {
                            db.collection("usuarios").document(creadorId).get().addOnSuccessListener(creadorSnap -> {
                                if (creadorSnap.exists()) {
                                    String nombreCreador = creadorSnap.getString("usuario");
                                    String fotoCreador = creadorSnap.getString("fotoPerfil");

                                    View creadorView = getLayoutInflater().inflate(R.layout.item_usuario_unido, null);
                                    TextView nombreTxt = creadorView.findViewById(R.id.nombreUsuario);
                                    ImageView imgUser = creadorView.findViewById(R.id.imagenUsuario);
                                    Button botonEliminar = creadorView.findViewById(R.id.botonEliminarUsuario);

                                    nombreTxt.setText((nombreCreador != null ? nombreCreador : "Sin nombre") + " (Creador)");
                                    if (fotoCreador != null && !fotoCreador.isEmpty()) {
                                        Glide.with(this).load(fotoCreador).into(imgUser);
                                    } else {
                                        imgUser.setImageResource(R.drawable.default_user);
                                    }

                                    botonEliminar.setVisibility(View.GONE); // No eliminar al creador
                                    layoutUsuarios.addView(creadorView);
                                }
                            });
                        }

                        // Mostrar participantes
                        List<String> participantes = (List<String>) docSnap.get("participantes");
                        if (participantes == null || participantes.isEmpty()) {
                            TextView vacio = new TextView(this);
                            vacio.setText("No hay participantes unidos");
                            layoutUsuarios.addView(vacio);
                            return;
                        }

                        for (String uid : participantes) {
                            if (uid.equals(creadorId)) continue; // Ya mostrado

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

                                    layoutUsuarios.addView(userView);
                                }
                            });
                        }
                    });
                }
            });


            // Botón chat
            botonChat.setOnClickListener(view -> {
                if (planId != null && nombre != null) {
                    Intent intentChat = new Intent(this, ChatPlan.class);
                    intentChat.putExtra("planId", planId);
                    intentChat.putExtra("nombre", nombre);
                    startActivity(intentChat);
                }
            });
        }
    }
    private void intentarUnirse(DocumentReference planRef, String userId, com.google.firebase.firestore.DocumentSnapshot documentSnapshot) {
        List<String> participantes = (List<String>) documentSnapshot.get("participantes");
        Long limite = documentSnapshot.getLong("limiteParticipantes");

        if (participantes == null) participantes = new ArrayList<>();

        if (participantes.contains(userId)) {
            Toast.makeText(this, "Ya estás en el plan", Toast.LENGTH_SHORT).show();
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
                });
    }

    public void irAOtraPantalla(View view) {
        finish();
    }


}
