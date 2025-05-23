package com.example.join.chats;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.join.R;
import com.example.join.plan.crearNuevoPlan;
import com.example.join.plan.listarPlanesCercanos;
import com.example.join.plan.menu;
import com.example.join.perfil.miPerfil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class BuscarUsuario extends AppCompatActivity {

    private EditText inputBusqueda;
    private RecyclerView recyclerUsuarios, recyclerChatsPrivados;
    private UsuarioAdapter usuarioAdapter;
    private ChatPrivadoAdapter chatAdapter;
    private List<UsuarioModelo> listaUsuarios;
    private List<ChatPrivadoModelo> listaChats;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.join.R.layout.activity_buscar_usuario);

        inputBusqueda = findViewById(com.example.join.R.id.inputBusqueda);
        recyclerUsuarios = findViewById(com.example.join.R.id.recyclerUsuarios);
        recyclerChatsPrivados = findViewById(com.example.join.R.id.recyclerChatsPrivados);

        recyclerUsuarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerChatsPrivados.setLayoutManager(new LinearLayoutManager(this));

        listaUsuarios = new ArrayList<>();
        listaChats = new ArrayList<>();

        usuarioAdapter = new UsuarioAdapter(this, listaUsuarios, com.example.join.R.layout.item_usuario_busqueda);
        chatAdapter = new ChatPrivadoAdapter(this, listaChats);

        recyclerUsuarios.setAdapter(usuarioAdapter);
        recyclerChatsPrivados.setAdapter(chatAdapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        inputBusqueda.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buscarUsuarios(s.toString().toLowerCase());
            }
        });

        cargarChatsPrivados();

        ImageView botonMapa = findViewById(com.example.join.R.id.imageView4);
        botonMapa.setOnClickListener(v -> {
            Intent intent = new Intent(BuscarUsuario.this, menu.class);
            startActivity(intent);
        });

        ImageView botonPerfil = findViewById(com.example.join.R.id.imageView8);
        botonPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(BuscarUsuario.this, miPerfil.class);
            startActivity(intent);
        });

        ImageView botonChat = findViewById(com.example.join.R.id.imageView2);
        botonChat.setOnClickListener(v -> {
            Intent intent = new Intent(BuscarUsuario.this, BuscarUsuario.class);
            startActivity(intent);
        });

        ImageView botonListas = findViewById(com.example.join.R.id.imageView10);
        botonListas.setOnClickListener(v -> {
            Intent intent = new Intent(BuscarUsuario.this, listarPlanesCercanos.class);
            startActivity(intent);
        });

        ImageView botonPlan = findViewById(R.id.imageView7);
        botonPlan.setOnClickListener(v -> {
            startActivity(new Intent(this, crearNuevoPlan.class));
        });

    }

    private void buscarUsuarios(String texto) {
        db.collection("usuarios")
                .get()
                .addOnSuccessListener(result -> {
                    listaUsuarios.clear();
                    for (QueryDocumentSnapshot doc : result) {
                        String nombre = doc.getString("usuario");
                        String id = doc.getId();
                        String fotoUrl = doc.getString("fotoPerfil");

                        if (id.equals(currentUserId)) continue;

                        if (nombre != null && nombre.toLowerCase().contains(texto)) {
                            listaUsuarios.add(new UsuarioModelo(id, nombre, fotoUrl));
                        }
                    }
                    usuarioAdapter.notifyDataSetChanged();
                });
    }

    private void cargarChatsPrivados() {
        listaChats.clear();

        db.collection("chats")
                .whereArrayContains("usuarios", currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot chatDoc : snapshot.getDocuments()) {
                        String chatId = chatDoc.getId();
                        List<String> usuarios = (List<String>) chatDoc.get("usuarios");

                        if (usuarios == null || usuarios.size() != 2) continue;

                        String otroId = usuarios.get(0).equals(currentUserId) ? usuarios.get(1) : usuarios.get(0);

                        db.collection("usuarios").document(otroId)
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    String nombre = userDoc.getString("usuario");
                                    String foto = userDoc.getString("fotoPerfil");

                                    listaChats.add(new ChatPrivadoModelo(chatId, otroId, nombre, foto));
                                    chatAdapter.notifyDataSetChanged();
                                });
                    }
                });
    }




}
