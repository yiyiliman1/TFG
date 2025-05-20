package com.example.join;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class miActividad extends AppCompatActivity {

    RecyclerView recyclerView;
    List<PlanItem> listaMisPlanes = new ArrayList<>();
    PlanAdapter adapter;

    FirebaseFirestore db;
    String currentUserId;
    FusedLocationProviderClient fusedLocationProviderClient;

    private static final int REQUEST_CODE_LOCATION = 1001;
    private double lastLat = 0.0;
    private double lastLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_actividad);

        recyclerView = findViewById(R.id.recyclerViewMisPlanes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        pedirUbicacionYListar();
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
                lastLat = location.getLatitude();
                lastLng = location.getLongitude();
                cargarMisPlanes(lastLat, lastLng);
            } else {
                Toast.makeText(this, "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarMisPlanes(double userLat, double userLng) {
        db.collection("planes").get().addOnSuccessListener(queryDocumentSnapshots -> {
            listaMisPlanes.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String estado = doc.getString("estado");
                Timestamp fechaHora = doc.getTimestamp("fechaHora");

                if (fechaHora != null && "activo".equals(estado)) {
                    if (fechaHora.toDate().before(new java.util.Date())) {
                        doc.getReference().update("estado", "finalizado");
                        continue;
                    }
                }

                if ("cancelado".equals(estado) || "finalizado".equals(estado)) continue;

                String creadorId = doc.getString("creadorId");
                List<String> participantes = (List<String>) doc.get("participantes");

                if ((creadorId != null && creadorId.equals(currentUserId)) ||
                        (participantes != null && participantes.contains(currentUserId))) {

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
                        listaMisPlanes.add(planItem);
                    }
                }
            }
            adapter = new PlanAdapter(listaMisPlanes, this, userLat, userLng, R.layout.activity_item_plan_cercano, true);

            recyclerView.setAdapter(adapter);
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error al cargar tus planes", Toast.LENGTH_SHORT).show()
        );
    }



    @Override
    public void onResume() {
        super.onResume();
        // Recargar lista al volver a la actividad
        if (lastLat != 0.0 && lastLng != 0.0) {
            cargarMisPlanes(lastLat, lastLng);
        } else {
            pedirUbicacionYListar();
        }
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
