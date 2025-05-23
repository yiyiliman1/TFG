package com.example.join.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.join.R;
import com.example.join.chats.BuscarUsuario;
import com.example.join.plan.crearNuevoPlan;
import com.example.join.plan.listarPlanesCercanos;
import com.example.join.plan.menu;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class PerfilUsuario extends AppCompatActivity {

    TextView textUsuario, textCorreo, textBiografia, textUbicacion, textCategoria1, textCategoria2, textCategoria3;
    ImageView imageViewFoto;
    Button btnAmistad;

    private String viewedUserId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.join.R.layout.activity_perfil_usuario);

        textUsuario = findViewById(com.example.join.R.id.textView30);
        textCorreo = findViewById(com.example.join.R.id.textView31);
        textBiografia = findViewById(com.example.join.R.id.editTextText4);
        textUbicacion = findViewById(com.example.join.R.id.editTextText7);
        imageViewFoto = findViewById(com.example.join.R.id.imageView29);
        textCategoria1 = findViewById(com.example.join.R.id.textCategoria1);
        textCategoria2 = findViewById(com.example.join.R.id.textCategoria2);
        textCategoria3 = findViewById(com.example.join.R.id.textCategoria3);
        btnAmistad = findViewById(com.example.join.R.id.btnAmistad);

        viewedUserId = getIntent().getStringExtra("usuarioId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (viewedUserId == null || viewedUserId.isEmpty()) {
            Toast.makeText(this, "Usuario no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!viewedUserId.equals(currentUserId)) {
            List<String> ids = new ArrayList<>();
            ids.add(currentUserId);
            ids.add(viewedUserId);
            Collections.sort(ids);
            String chatId = ids.get(0) + "_" + ids.get(1);

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Primero, verificar si hay rechazo activo
            db.collection("rechazos").document(chatId).get().addOnSuccessListener(rechazoDoc -> {
                if (rechazoDoc.exists()) {
                    Timestamp rechazadoHasta = rechazoDoc.getTimestamp("rechazadoHasta");
                    if (rechazadoHasta != null && rechazadoHasta.toDate().after(new Date())) {
                        btnAmistad.setVisibility(View.VISIBLE);
                        btnAmistad.setEnabled(false);
                        btnAmistad.setText("Solicitud rechazada");
                        return;
                    }
                }

                // Si no hay rechazo, verificar estado del chat
                db.collection("chats").document(chatId)
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                Boolean confirmado = doc.getBoolean("confirmado");
                                if (Boolean.TRUE.equals(confirmado)) {
                                    btnAmistad.setVisibility(View.VISIBLE);
                                    btnAmistad.setEnabled(false);
                                    btnAmistad.setText("Ya sois amigos");
                                } else {
                                    btnAmistad.setVisibility(View.VISIBLE);
                                    btnAmistad.setEnabled(false);
                                    btnAmistad.setText("Solicitud enviada");
                                }
                            } else {
                                btnAmistad.setVisibility(View.VISIBLE);
                                btnAmistad.setEnabled(true);
                                btnAmistad.setText("Solicitar amistad");
                                btnAmistad.setOnClickListener(v -> enviarSolicitudPorChat());
                            }
                        })
                        .addOnFailureListener(e -> {
                            btnAmistad.setVisibility(View.VISIBLE);
                            btnAmistad.setEnabled(true);
                            btnAmistad.setText("Solicitar amistad");
                            btnAmistad.setOnClickListener(v -> enviarSolicitudPorChat());
                        });

            });
        }

        FirebaseFirestore.getInstance().collection("usuarios").document(viewedUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        textUsuario.setText(doc.getString("usuario"));
                        textCorreo.setText(doc.getString("email"));
                        textBiografia.setText(doc.getString("biografia"));
                        textUbicacion.setText(doc.getString("ubicacion"));

                        String url = doc.getString("fotoPerfil");
                        if (url != null && !url.isEmpty()) {
                            Glide.with(this).load(url).circleCrop().into(imageViewFoto);
                        }

                        Object interesesObj = doc.get("intereses");
                        if (interesesObj instanceof List) {
                            List<String> intereses = (List<String>) interesesObj;
                            textCategoria1.setText(intereses.size() > 0 ? intereses.get(0) : "");
                            textCategoria2.setText(intereses.size() > 1 ? intereses.get(1) : "");
                            textCategoria3.setText(intereses.size() > 2 ? intereses.get(2) : "");
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show());

        ImageView botonMapa = findViewById(com.example.join.R.id.imageView4);
        botonMapa.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilUsuario.this, menu.class);
            startActivity(intent);
        });

        ImageView botonPerfil = findViewById(com.example.join.R.id.imageView8);
        botonPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilUsuario.this, miPerfil.class);
            startActivity(intent);
        });

        ImageView botonChat = findViewById(com.example.join.R.id.imageView2);
        botonChat.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilUsuario.this, BuscarUsuario.class);
            startActivity(intent);
        });

        ImageView botonListas = findViewById(com.example.join.R.id.imageView10);
        botonListas.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilUsuario.this, listarPlanesCercanos.class);
            startActivity(intent);
        });

        ImageView botonPlan = findViewById(R.id.imageView7);
        botonPlan.setOnClickListener(v -> {
            startActivity(new Intent(this, crearNuevoPlan.class));
        });

    }

    private void enviarSolicitudPorChat() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> ids = new ArrayList<>();
        ids.add(currentUserId);
        ids.add(viewedUserId);
        Collections.sort(ids);
        String chatId = ids.get(0) + "_" + ids.get(1);

        DocumentReference chatRef = db.collection("chats").document(chatId);
        chatRef.get().addOnSuccessListener(chatSnapshot -> {
            if (!chatSnapshot.exists()) {
                Map<String, Object> chatData = new HashMap<>();
                chatData.put("usuarios", ids);
                chatData.put("createdAt", Timestamp.now());
                chatData.put("confirmado", false);
                chatRef.set(chatData);
            }

            db.collection("usuarios").document(currentUserId).get().addOnSuccessListener(userDoc -> {
                String autorNombre = userDoc.getString("usuario");

                Map<String, Object> mensaje = new HashMap<>();
                mensaje.put("texto", "¡Hola! ¿Quieres ser mi amigo?");
                mensaje.put("autorId", currentUserId);
                mensaje.put("autorNombre", autorNombre);
                mensaje.put("timestamp", FieldValue.serverTimestamp());
                mensaje.put("tipo", "solicitud_amistad");

                chatRef.collection("mensajes")
                        .add(mensaje)
                        .addOnSuccessListener(doc -> {
                            Toast.makeText(this, "Solicitud de amistad enviada", Toast.LENGTH_SHORT).show();
                            btnAmistad.setText("Solicitud enviada");
                            btnAmistad.setEnabled(false);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al enviar solicitud", Toast.LENGTH_SHORT).show();
                        });

            }).addOnFailureListener(e -> {
                Toast.makeText(this, "No se pudo obtener tu nombre", Toast.LENGTH_SHORT).show();
            });

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al verificar el chat", Toast.LENGTH_SHORT).show();
        });
    }


}
