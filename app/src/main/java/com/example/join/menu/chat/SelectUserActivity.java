package com.example.join.menu.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.join.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SelectUserActivity extends AppCompatActivity {

    private RecyclerView userRecyclerView;
    private SelectUserAdapter adapter;
    private List<UserModel> userList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user);

        userRecyclerView = findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        adapter = new SelectUserAdapter(userList, user -> {
            Intent intent = new Intent(SelectUserActivity.this, ChatActivity.class);
            intent.putExtra("receiverId", user.getId());
            intent.putExtra("username", user.getUsername());
            startActivity(intent);
            finish();
        });

        userRecyclerView.setAdapter(adapter);

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        db.collection("usuarios")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String nombre = doc.getString("usuario");
                        if (nombre != null) {
                            userList.add(new UserModel(id, nombre));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show());
    }
}
