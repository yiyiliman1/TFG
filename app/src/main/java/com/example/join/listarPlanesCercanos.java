package com.example.join;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class listarPlanesCercanos extends AppCompatActivity {

    RecyclerView recyclerView;
    List<PlanItem> listaPlanes = new ArrayList<>();
    PlanAdapter adapter;
    TextView textoFiltroActivo;


    private double userLat = 0.0;
    private double userLng = 0.0;

    FirebaseFirestore db;
    FusedLocationProviderClient fusedLocationProviderClient;

    private static final int REQUEST_CODE_LOCATION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_planes_cercanos);

        Button botonMiActividad = findViewById(R.id.botonMiActividad);
        botonMiActividad.setOnClickListener(v -> {
            Intent intent = new Intent(this, miActividad.class);
            startActivity(intent);
        });

        ImageView botonMenu = findViewById(R.id.imageView4);
        botonMenu.setOnClickListener(v -> {
            startActivity(new Intent(this, menu.class));
        });

        ImageView botonPerfil = findViewById(R.id.imageView8);
        botonPerfil.setOnClickListener(v -> {
            startActivity(new Intent(this, miPerfil.class));
        });

        ImageView filtroBtn = findViewById(R.id.imageView5);
        filtroBtn.setOnClickListener(v -> mostrarDialogoFiltros());

        recyclerView = findViewById(R.id.recyclerViewPlanes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        textoFiltroActivo = findViewById(R.id.textoFiltroActivo);

        db = FirebaseFirestore.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        pedirUbicacionYListar();

        ImageView backButton = findViewById(R.id.imageView);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, menu.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }

    private void mostrarDialogoFiltros() {
        String[] opciones = {"Cercanía Distancia", "Cercanía Fecha", "Categoría", "Limpiar Filtro"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Ordenar/filtrar planes")
                .setItems(opciones, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            ordenarPorDistancia();
                            break;
                        case 1:
                            ordenarPorFecha();
                            break;
                        case 2:
                            mostrarDialogoCategorias();
                            break;
                        case 3:
                            textoFiltroActivo.setVisibility(View.GONE);
                            adapter = new PlanAdapter(listaPlanes, this, userLat, userLng, R.layout.activity_item_plan_cercano,true);

                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            break;
                    }
                }).show();
    }


    private void ordenarPorDistancia() {
        textoFiltroActivo.setText("Filtrando por cercanía...");
        textoFiltroActivo.setVisibility(View.VISIBLE);
        listaPlanes.sort((a, b) -> {
            float[] resultsA = new float[1];
            float[] resultsB = new float[1];
            Location.distanceBetween(userLat, userLng, a.getLatitud(), a.getLongitud(), resultsA);
            Location.distanceBetween(userLat, userLng, b.getLatitud(), b.getLongitud(), resultsB);
            return Float.compare(resultsA[0], resultsB[0]);
        });
        adapter.notifyDataSetChanged();
    }

    private void ordenarPorFecha() {
        textoFiltroActivo.setText("Filtrando por fecha próxima...");
        textoFiltroActivo.setVisibility(View.VISIBLE);
        listaPlanes.sort((a, b) -> {
            if (a.getFechaHora() == null || b.getFechaHora() == null) return 0;
            return a.getFechaHora().compareTo(b.getFechaHora());
        });
        adapter.notifyDataSetChanged();
    }

    private void mostrarDialogoCategorias() {
        String[] categorias = {"Cena", "Fiesta", "Deporte", "Cultura", "Excursión", "Videojuegos"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Elige una categoría")
                .setItems(categorias, (dialog, which) -> {
                    filtrarPorCategoria(categorias[which]);
                }).show();
    }

    private void filtrarPorCategoria(String categoriaSeleccionada) {
        textoFiltroActivo.setText("Filtrando por categoría '" + categoriaSeleccionada + "'...");
        textoFiltroActivo.setVisibility(View.VISIBLE);
        List<PlanItem> planesFiltrados = new ArrayList<>();
        for (PlanItem plan : listaPlanes) {
            if (plan.getCategoria().equalsIgnoreCase(categoriaSeleccionada)) {
                planesFiltrados.add(plan);
            }
        }
        adapter = new PlanAdapter(planesFiltrados, this, userLat, userLng, R.layout.activity_item_plan_cercano, true);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void pedirUbicacionYListar() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLat = location.getLatitude();
                userLng = location.getLongitude();
                cargarPlanes(userLat, userLng);
            } else {
                Toast.makeText(this, "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarPlanes(double userLat, double userLng) {
        db.collection("planes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaPlanes.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Timestamp fechaHora = doc.getTimestamp("fechaHora");
                        String estado = doc.getString("estado");

                        if (fechaHora != null && "activo".equals(estado)) {
                            if (fechaHora.toDate().before(new Date())) {
                                doc.getReference().update("estado", "finalizado");
                                continue;
                            }
                        }

                        if ("cancelado".equals(estado) || "finalizado".equals(estado)) {
                            continue;
                        }

                        String nombre = doc.getString("nombre");
                        String categoria = doc.getString("categoria");
                        Double lat = doc.getDouble("latitud");
                        Double lng = doc.getDouble("longitud");
                        String descripcion = doc.getString("descripcion");
                        String direccion = doc.getString("direccion");
                        String fotoUrl = doc.getString("fotoUrl");

                        if (nombre != null && categoria != null && lat != null && lng != null) {
                            PlanItem planItem = new PlanItem(nombre, categoria, lat, lng, descripcion, direccion);
                            planItem.setId(doc.getId());
                            planItem.setFotoUrl(fotoUrl);
                            if (fechaHora != null) {
                                planItem.setFechaHora(fechaHora.toDate());
                            }
                            listaPlanes.add(planItem);
                        }
                    }

                    adapter = new PlanAdapter(listaPlanes, this, userLat, userLng, R.layout.activity_item_plan_cercano,true);

                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar planes", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pedirUbicacionYListar();
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
        }
    }
}
