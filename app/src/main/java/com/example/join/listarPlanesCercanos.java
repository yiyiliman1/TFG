package com.example.join;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class listarPlanesCercanos extends AppCompatActivity {

    RecyclerView recyclerView;
    List<PlanItem> listaPlanes = new ArrayList<>();
    PlanAdapter adapter;

    FirebaseFirestore db;
    FusedLocationProviderClient fusedLocationProviderClient;

    private static final int REQUEST_CODE_LOCATION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        setContentView(R.layout.activity_listar_planes_cercanos);

        ImageView botonMenu = findViewById(R.id.imageView4);

        botonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(listarPlanesCercanos.this, menu.class);
                startActivity(intent);
            }
        });
        ImageView botonPerfil = findViewById(R.id.imageView8);
        botonPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(listarPlanesCercanos.this, miPerfil.class);
                startActivity(intent);
            }
        });

        recyclerView = findViewById(R.id.recyclerViewPlanes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        pedirUbicacionYListar();

        ImageView backButton = findViewById(R.id.imageView);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(listarPlanesCercanos.this, menu.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });

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
                double userLat = location.getLatitude();
                double userLng = location.getLongitude();
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
                        String nombre = doc.getString("nombre");
                        String categoria = doc.getString("categoria");
                        Double lat = doc.getDouble("latitud");
                        Double lng = doc.getDouble("longitud");

                        if (nombre != null && categoria != null && lat != null && lng != null) {
                            listaPlanes.add(new PlanItem(nombre, categoria, lat, lng));
                        }
                    }
                    adapter = new PlanAdapter(listaPlanes, this, userLat, userLng);
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
