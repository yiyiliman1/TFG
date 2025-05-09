// ChatActivity.java (actualizado con RecyclerView funcional)
package com.example.join.menu.chat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.join.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private TextView chatHeader;
    private EditText messageInput;
    private ImageButton sendButton;
    private RecyclerView messageList;

    private String receiverId;
    private String currentUserId;
    private String chatId;

    private FirebaseFirestore db;
    private MensajeAdapter adapter;
    private List<Mensaje> mensajes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        receiverId = getIntent().getStringExtra("receiverId");
        String username = getIntent().getStringExtra("username");

        if (receiverId == null || username == null) {
            Toast.makeText(this, "Error: datos del usuario no recibidos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatHeader = findViewById(R.id.nonbreAmigo);
        chatHeader.setText("Chat con " + username);

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messageList = findViewById(R.id.messageList);

        chatId = generateChatId(currentUserId, receiverId);

        adapter = new MensajeAdapter(mensajes, currentUserId);
        messageList.setLayoutManager(new LinearLayoutManager(this));
        messageList.setAdapter(adapter);

        sendButton.setOnClickListener(v -> sendMessage());

        escucharMensajes();
    }

    private String generateChatId(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        Map<String, Object> mensaje = new HashMap<>();
        mensaje.put("senderId", currentUserId);
        mensaje.put("text", text);
        mensaje.put("timestamp", new Date());

        db.collection("chats")
                .document(chatId)
                .collection("mensajes")
                .add(mensaje)
                .addOnSuccessListener(documentReference -> messageInput.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(ChatActivity.this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show());
    }

    private void escucharMensajes() {
        db.collection("chats")
                .document(chatId)
                .collection("mensajes")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    for (DocumentChange dc : value.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Mensaje m = dc.getDocument().toObject(Mensaje.class);
                            mensajes.add(m);
                            adapter.notifyItemInserted(mensajes.size() - 1);
                            messageList.scrollToPosition(mensajes.size() - 1);
                        }
                    }
                });
    }
}