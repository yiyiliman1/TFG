package com.example.join;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_plan); // Reutilizar este layout del plan grupal

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        mensajeInput = findViewById(R.id.editTextMensaje);
        enviarBtn = findViewById(R.id.botonEnviarMensaje);
        listaMensajes = findViewById(R.id.listaMensajes);
        tituloChat = findViewById(R.id.textViewTituloChat);

        chatId = getIntent().getStringExtra("chatId");
        otroId = getIntent().getStringExtra("usuarioId");
        nombre = getIntent().getStringExtra("nombre");

        tituloChat.setText(nombre);

        mensajes = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, mensajes);
        listaMensajes.setAdapter(chatAdapter);

        escucharMensajes();

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
                        mensajes.add(msg);
                    }
                    chatAdapter.notifyDataSetChanged();
                    listaMensajes.setSelection(mensajes.size() - 1);
                });
    }

    private void enviarMensaje() {
        String texto = mensajeInput.getText().toString().trim();
        if (texto.isEmpty()) return;

        String userId = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(userId).get().addOnSuccessListener(doc -> {
            String nombre = doc.getString("usuario");

            Map<String, Object> mensaje = new HashMap<>();
            mensaje.put("texto", texto);
            mensaje.put("autorId", userId);
            mensaje.put("autorNombre", nombre);
            mensaje.put("timestamp", Timestamp.now());
            mensaje.put("tipo", "normal");

            db.collection("chats").document(chatId).collection("mensajes")
                    .add(mensaje)
                    .addOnSuccessListener(r -> mensajeInput.setText(""));
        });
    }
}
