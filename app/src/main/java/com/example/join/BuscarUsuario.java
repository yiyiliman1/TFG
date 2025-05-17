package com.example.join;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.*;

public class BuscarUsuario extends AppCompatActivity {

    private EditText inputBusqueda;
    private RecyclerView recyclerUsuarios;
    private UsuarioAdapter usuarioAdapter;
    private List<UsuarioModelo> listaUsuarios;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_usuario);

        inputBusqueda = findViewById(R.id.inputBusqueda);
        recyclerUsuarios = findViewById(R.id.recyclerUsuarios);
        recyclerUsuarios.setLayoutManager(new LinearLayoutManager(this));

        listaUsuarios = new ArrayList<>();
        usuarioAdapter = new UsuarioAdapter(this, listaUsuarios);
        recyclerUsuarios.setAdapter(usuarioAdapter);

        db = FirebaseFirestore.getInstance();

        inputBusqueda.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buscarUsuarios(s.toString().toLowerCase());
            }
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

                        if (nombre != null && nombre.toLowerCase().contains(texto)) {
                            listaUsuarios.add(new UsuarioModelo(id, nombre, fotoUrl));
                        }
                    }
                    usuarioAdapter.notifyDataSetChanged();
                });
    }
}
