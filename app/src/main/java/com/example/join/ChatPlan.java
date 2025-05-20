package com.example.join;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatPlan extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String planId, nombrePlan;

    private EditText mensajeInput;
    private Button enviarBtn;
    private ListView listaMensajes;
    private ChatAdapter chatAdapter;
    private List<MensajeChat> mensajes;
    private TextView tituloChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_plan);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        mensajeInput = findViewById(R.id.editTextMensaje);
        enviarBtn = findViewById(R.id.botonEnviarMensaje);
        listaMensajes = findViewById(R.id.listaMensajes);
        tituloChat = findViewById(R.id.textViewTituloChat);

        mensajes = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, mensajes);
        listaMensajes.setAdapter(chatAdapter);

        planId = getIntent().getStringExtra("planId");
        nombrePlan = getIntent().getStringExtra("nombre");

        if (planId == null || planId.isEmpty()) {
            Toast.makeText(this, "Error: ID del plan no válido", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (nombrePlan != null && !nombrePlan.isEmpty()) {
            tituloChat.setText(nombrePlan);
        } else {
            tituloChat.setText("Chat del Plan");
        }

        escucharMensajes();

        enviarBtn.setOnClickListener(v -> enviarMensaje());
    }

    private void escucharMensajes() {
        db.collection("planes")
                .document(planId)
                .collection("mensajes")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    mensajes.clear();
                    for (DocumentSnapshot doc : snapshots) {
                        MensajeChat msg = doc.toObject(MensajeChat.class);
                        if (msg != null) mensajes.add(msg);
                    }
                    chatAdapter.notifyDataSetChanged();
                    listaMensajes.setSelection(mensajes.size() - 1);
                });
    }

    private void enviarMensaje() {
        String texto = mensajeInput.getText().toString().trim();
        if (texto.isEmpty() || auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();

        db.collection("usuarios").document(userId).get().addOnSuccessListener(doc -> {
            String nombre = doc.getString("usuario");
            if (nombre == null) nombre = "Anónimo";

            Map<String, Object> mensaje = new HashMap<>();
            mensaje.put("texto", texto);
            mensaje.put("autorId", userId);
            mensaje.put("autorNombre", nombre);
            mensaje.put("timestamp", Timestamp.now());

            db.collection("planes")
                    .document(planId)
                    .collection("mensajes")
                    .add(mensaje)
                    .addOnSuccessListener(r -> mensajeInput.setText(""))
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show());
        });
    }

    public void irAOtraPantalla(View view) {
        Intent intent = new Intent(this, menu.class);
        startActivity(intent);
    }

}
