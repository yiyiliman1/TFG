package com.example.join;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerfilUsuario extends AppCompatActivity {

    TextView textUsuario, textCorreo, textBiografia, textUbicacion, textCategoria1, textCategoria2, textCategoria3;
    ImageView imageViewFoto;
    Button btnAmistad;

    private String viewedUserId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);

        textUsuario = findViewById(R.id.textView30);
        textCorreo = findViewById(R.id.textView31);
        textBiografia = findViewById(R.id.editTextText4);
        textUbicacion = findViewById(R.id.editTextText7);
        imageViewFoto = findViewById(R.id.imageView29);
        textCategoria1 = findViewById(R.id.textCategoria1);
        textCategoria2 = findViewById(R.id.textCategoria2);
        textCategoria3 = findViewById(R.id.textCategoria3);
        btnAmistad = findViewById(R.id.btnAmistad);


        viewedUserId = getIntent().getStringExtra("usuarioId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (viewedUserId == null || viewedUserId.isEmpty()) {
            Toast.makeText(this, "Usuario no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mostrar botón solo si no es tu propio perfil
        if (!viewedUserId.equals(currentUserId)) {
            btnAmistad.setVisibility(View.VISIBLE);
            btnAmistad.setOnClickListener(v -> enviarSolicitudPorChat());
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
    }

    private void enviarSolicitudPorChat() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> ids = new ArrayList<>();
        ids.add(currentUserId);
        ids.add(viewedUserId);
        Collections.sort(ids);
        String chatId = ids.get(0) + "_" + ids.get(1);

        // Paso 1: Crear el documento del chat si no existe
        DocumentReference chatRef = db.collection("chats").document(chatId);
        chatRef.get().addOnSuccessListener(chatSnapshot -> {
            if (!chatSnapshot.exists()) {
                Map<String, Object> chatData = new HashMap<>();
                chatData.put("usuarios", ids);
                chatData.put("createdAt", Timestamp.now());
                chatRef.set(chatData);
            }

            // Paso 2: Obtener el nombre del autor y enviar el mensaje de solicitud
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
