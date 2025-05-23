package com.example.join.chats;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.join.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class ChatPrivado extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String chatId, otroId, nombre;
    private EditText mensajeInput;
    private Button enviarBtn;
    private ListView listaMensajes;
    private ChatAdapter chatAdapter;
    private List<MensajeChat> mensajes;
    private TextView tituloChat;

    private boolean puedeHablar = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.join.R.layout.activity_chat_plan);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        mensajeInput = findViewById(com.example.join.R.id.editTextMensaje);
        enviarBtn = findViewById(com.example.join.R.id.botonEnviarMensaje);
        listaMensajes = findViewById(com.example.join.R.id.listaMensajes);
        tituloChat = findViewById(R.id.textViewTituloChat);

        chatId = getIntent().getStringExtra("chatId");
        otroId = getIntent().getStringExtra("usuarioId");
        nombre = getIntent().getStringExtra("nombre");

        tituloChat.setText(nombre);

        mensajes = new ArrayList<>();


        db.collection("chats").document(chatId).get().addOnSuccessListener(doc -> {
            boolean confirmado = doc.exists() && Boolean.TRUE.equals(doc.getBoolean("confirmado"));
            puedeHablar = confirmado;

            // Crear adapter solo después de saber si está confirmado
            chatAdapter = new ChatAdapter(this, mensajes);
            chatAdapter.setChatConfirmado(confirmado);
            listaMensajes.setAdapter(chatAdapter);

            if (!puedeHablar) {
                enviarBtn.setEnabled(false);
                mensajeInput.setHint("Podréis chatear una vez seáis amigos");
            } else {
                enviarBtn.setEnabled(true);
                mensajeInput.setHint("Escribe un mensaje...");
            }

            escucharMensajes();
        });

        enviarBtn.setOnClickListener(v -> enviarMensaje());
    }

    private void escucharMensajes() {
        db.collection("chats").document(chatId).collection("mensajes")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;
                    mensajes.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        MensajeChat msg = doc.toObject(MensajeChat.class);
                        if (msg != null && doc.contains("timestamp")) {
                            mensajes.add(msg);
                        }
                    }

                    chatAdapter.notifyDataSetChanged();
                    listaMensajes.setSelection(mensajes.size() - 1);
                });
    }

    private void enviarMensaje() {
        if (!puedeHablar) {
            Toast.makeText(this, "Solo podréis conversar una vez seáis amigos.", Toast.LENGTH_SHORT).show();
            return;
        }

        String texto = mensajeInput.getText().toString().trim();
        if (texto.isEmpty()) return;

        String userId = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(userId).get().addOnSuccessListener(doc -> {
            String nombre = doc.getString("usuario");

            Map<String, Object> mensaje = new HashMap<>();
            mensaje.put("texto", texto);
            mensaje.put("autorId", userId);
            mensaje.put("autorNombre", nombre);
            mensaje.put("timestamp", FieldValue.serverTimestamp());
            mensaje.put("tipo", "normal");

            db.collection("chats").document(chatId).collection("mensajes")
                    .add(mensaje)
                    .addOnSuccessListener(r -> mensajeInput.setText(""));
        });
    }
}
