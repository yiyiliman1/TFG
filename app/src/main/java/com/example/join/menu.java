package com.example.join;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;



public class menu extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    EditText txtLat, txtLong;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    ImageView botonMas;

    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.menu), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Mostrar mensaje si viene del intent
        String mensaje = getIntent().getStringExtra("mensaje_exito");
        if (mensaje != null) {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        }

        botonMas = findViewById(R.id.imageView7);

        botonMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ir a la pantalla de crear plan
                Intent intent = new Intent(menu.this, crearNuevoPlan.class);
                startActivity(intent);
            }
        });

        //txtLat=findViewById(R.id.txtLatitud);
       // txtLong=findViewById(R.id.txtLongitud);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();
        cargarPlanesDesdeFirestore();

        ImageView botonListas = findViewById(R.id.imageView10);

        botonListas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(menu.this, listarPlanesCercanos.class);
                startActivity(intent);
            }
        });
        ImageView botonPerfil = findViewById(R.id.imageView8);
        botonPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(menu.this, miPerfil.class);
                startActivity(intent);
            }
        });


    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Activar punto azul si hay permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Estilo de mapa
        mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        // Mover cámara a posición inicial si quieres
        LatLng espana = new LatLng(40.4943143, -3.6568315);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(espana, 12));
    }



    private void getCurrentLocation(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION
            );
            return;
        }

       /* fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if(location != null){
                txtLat.setText("Latitud" + location.getLatitude());
                txtLong.setText("Longitud" + location.getLongitude());
            }else{
                txtLat.setText("NO se pudo obtener la ubicacion");
            }
        });*/


    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

       /* if(requestCode== REQUEST_CODE_LOCATION_PERMISSION && grantResults.length>0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation();
            }else{
                txtLat.setText("Permiso de Ubicación Denegado");
            }
        }*/

    }

    private void cargarPlanesDesdeFirestore() {
        db.collection("planes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Double lat = doc.getDouble("latitud");
                        Double lng = doc.getDouble("longitud");
                        String nombre = doc.getString("nombre");

                        if (lat != null && lng != null && nombre != null) {
                            LatLng ubicacion = new LatLng(lat, lng);
                            mMap.addMarker(new MarkerOptions()
                                    .position(ubicacion)
                                    .title(nombre));
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando planes", Toast.LENGTH_SHORT).show()
                );
    }



}

