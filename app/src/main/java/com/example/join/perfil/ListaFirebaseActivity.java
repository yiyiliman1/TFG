package com.example.join.perfil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.join.plan.PlanAdapter;
import com.example.join.plan.PlanItem;
import com.example.join.R;
import com.example.join.chats.UsuarioAdapter;
import com.example.join.chats.UsuarioModelo;
import com.example.join.chats.BuscarUsuario;
import com.example.join.plan.listarPlanesCercanos;
import com.example.join.plan.menu;
import com.example.join.plan.crearNuevoPlan;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class ListaFirebaseActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private PlanAdapter planAdapter;
    private UsuarioAdapter usuarioAdapter;
    private TextView textTituloLista, textSinResultados;

    private String tipo;
    private int layoutId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.join.R.layout.activity_lista_firebase);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(com.example.join.R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        textTituloLista = findViewById(com.example.join.R.id.textTituloLista);
        textSinResultados = findViewById(com.example.join.R.id.textSinResultados);
        textSinResultados.setVisibility(View.GONE);



        tipo = getIntent().getStringExtra("tipo");
        if (tipo == null || mAuth.getCurrentUser() == null) {
            finish();
            return;
        }

        layoutId = getLayoutForTipo(tipo);

        // Asignar título dinámico
        switch (tipo) {
            case "planes_creados":
                textTituloLista.setText("Planes Creados");
                cargarPlanesCreados();
                break;
            case "planes_unidos":
                textTituloLista.setText("Planes Unidos");
                cargarPlanesUnidos();
                break;
            case "amigos":
                textTituloLista.setText("Lista de Amigos");
                cargarAmigos();
                break;
        }



        ImageView botonMapa = findViewById(com.example.join.R.id.imageView4);
        botonMapa.setOnClickListener(v -> {
            Intent intent = new Intent(ListaFirebaseActivity.this, menu.class);
            startActivity(intent);
        });

        ImageView botonPerfil = findViewById(com.example.join.R.id.imageView8);
        botonPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(ListaFirebaseActivity.this, miPerfil.class);
            startActivity(intent);
        });

        ImageView botonChat = findViewById(com.example.join.R.id.imageView2);
        botonChat.setOnClickListener(v -> {
            Intent intent = new Intent(ListaFirebaseActivity.this, BuscarUsuario.class);
            startActivity(intent);
        });

        ImageView botonListas = findViewById(com.example.join.R.id.imageView10);
        botonListas.setOnClickListener(v -> {
            Intent intent = new Intent(ListaFirebaseActivity.this, listarPlanesCercanos.class);
            startActivity(intent);
        });

        ImageView botonPlan = findViewById(com.example.join.R.id.imageView7);
        botonPlan.setOnClickListener(v -> {
            startActivity(new Intent(this, crearNuevoPlan.class));
        });

    }

    private int getLayoutForTipo(String tipo) {
        switch (tipo) {
            case "planes_creados":
            case "planes_unidos":
                return com.example.join.R.layout.item_plan_lista;
            case "amigos":
                return com.example.join.R.layout.item_amigo_lista;
            default:
                return com.example.join.R.layout.item_plan_lista; // Fallback por si acaso
        }
    }

    private void cargarPlanesCreados() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("planes")
                .whereEqualTo("creadorId", uid)
                .get()
                .addOnSuccessListener(query -> mostrarPlanes(query.getDocuments()));
    }

    private void cargarPlanesUnidos() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("planes")
                .whereArrayContains("participantes", uid)
                .get()
                .addOnSuccessListener(query -> mostrarPlanes(query.getDocuments()));
    }

    private void mostrarPlanes(List<DocumentSnapshot> docs) {
        if (docs.isEmpty()) {
            textSinResultados.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        textSinResultados.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        List<PlanItem> planes = new ArrayList<>();
        for (DocumentSnapshot doc : docs) {
            String nombre = doc.getString("nombre");
            String categoria = doc.getString("categoria");
            double lat = doc.getDouble("latitud");
            double lng = doc.getDouble("longitud");

            PlanItem plan = new PlanItem(nombre, categoria, lat, lng);
            plan.setId(doc.getId());
            plan.setDescripcion(doc.getString("descripcion"));
            plan.setDireccion(doc.getString("direccion"));
            plan.setFotoUrl(doc.getString("fotoUrl"));
            plan.setEstado(doc.getString("estado"));


            Date fecha = doc.getDate("fechaHora");
            if (fecha != null) plan.setFechaHora(fecha);

            planes.add(plan);
        }

        planAdapter = new PlanAdapter(planes, this, 0, 0, layoutId, false);

        recyclerView.setAdapter(planAdapter);
    }

    private void cargarAmigos() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<String> idsAmigos = (List<String>) doc.get("amigos");

                        if (idsAmigos == null || idsAmigos.isEmpty()) {
                            textSinResultados.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            return;
                        }

                        List<UsuarioModelo> usuarios = new ArrayList<>();
                        List<List<String>> chunks = dividirLista(idsAmigos, 10);

                        for (List<String> sublista : chunks) {
                            db.collection("usuarios")
                                    .whereIn(FieldPath.documentId(), sublista)
                                    .get()
                                    .addOnSuccessListener(query -> {
                                        for (DocumentSnapshot amigoDoc : query.getDocuments()) {
                                            String id = amigoDoc.getId();
                                            String nombre = amigoDoc.getString("usuario");
                                            String foto = amigoDoc.getString("fotoPerfil");
                                            usuarios.add(new UsuarioModelo(id, nombre, foto));
                                        }

                                        if (!usuarios.isEmpty()) {
                                            textSinResultados.setVisibility(View.GONE);
                                            recyclerView.setVisibility(View.VISIBLE);
                                            usuarioAdapter = new UsuarioAdapter(this, usuarios, R.layout.item_usuario_busqueda);
                                            recyclerView.setAdapter(usuarioAdapter);
                                        }
                                    });
                        }
                    }
                });
    }

    private List<List<String>> dividirLista(List<String> lista, int tamañoMaximo) {
        List<List<String>> partes = new ArrayList<>();
        for (int i = 0; i < lista.size(); i += tamañoMaximo) {
            partes.add(lista.subList(i, Math.min(i + tamañoMaximo, lista.size())));
        }
        return partes;
    }


}
