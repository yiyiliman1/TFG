// Chat_List.java (Actualizado con botón de nuevo chat)
package com.example.join.menu.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.join.R;
import java.util.ArrayList;
import java.util.List;

public class Chat_List extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter adapter;
    private List<ChatModel> chatList;
    private ImageButton newChatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatList = new ArrayList<>();
        chatList.add(new ChatModel("1", "Laura", "¿Nos vemos mañana?"));
        chatList.add(new ChatModel("2", "Carlos", "Te mandé la ubicación"));
        chatList.add(new ChatModel("3", "Andrea", "Ya llegué 💬"));
        chatList.add(new ChatModel("4", "Julián", "ok"));

        adapter = new ChatAdapter(this, chatList);
        chatRecyclerView.setAdapter(adapter);

        newChatButton = findViewById(R.id.btn_new_chat);
        newChatButton.setOnClickListener(v -> {
            Intent intent = new Intent(Chat_List.this, SelectUserActivity.class);
            startActivity(intent);
        });
    }
}
