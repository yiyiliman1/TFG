package com.example.join;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
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

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class menu extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap mMap;
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

        String mensaje = getIntent().getStringExtra("mensaje_exito");
        if (mensaje != null) {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        }

        botonMas = findViewById(R.id.imageView7);
        botonMas.setOnClickListener(v -> {
            Intent intent = new Intent(menu.this, crearNuevoPlan.class);
            startActivity(intent);
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getCurrentLocation();
        cargarPlanesDesdeFirestore();

        ImageView botonListas = findViewById(R.id.imageView10);
        botonListas.setOnClickListener(v -> {
            Intent intent = new Intent(menu.this, listarPlanesCercanos.class);
            startActivity(intent);
        });

        ImageView botonChat = findViewById(R.id.imageView2);
        botonChat.setOnClickListener(v -> {
            Intent intent = new Intent(menu.this, BuscarUsuario.class);
            startActivity(intent);
        });

        ImageView botonPerfil = findViewById(R.id.imageView8);
        botonPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(menu.this, miPerfil.class);
            startActivity(intent);
        });

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyAYs-3eObEiGutUhRq72l3n4BWcosHCzvw");
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteFragment.setCountries("ES");
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    LatLng latLng = place.getLatLng();
                    if (latLng != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    String mensaje = status.getStatusMessage();
                    if (mensaje != null && !mensaje.isEmpty()) {
                        Toast.makeText(menu.this, "Error: " + mensaje, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            View fragmentView = autocompleteFragment.getView();
            if (fragmentView != null) {
                EditText editText = fragmentView.findViewById(
                        com.google.android.libraries.places.R.id.places_autocomplete_search_input
                );
                editText.setHint("Buscar ubicación...");
            }
        }

        ImageView btnCentrar = findViewById(R.id.btn_centrar_ubicacion);
        btnCentrar.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(menu.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(menu.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(menu.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_LOCATION_PERMISSION);
                return;
            }

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null && mMap != null) {
                    LatLng ubicacionActual = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacionActual, 16));
                } else {
                    Toast.makeText(menu.this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        LatLng espana = new LatLng(40.4943143, -3.6568315);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(espana, 12));
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION
            );
        }
    }

    private void cargarPlanesDesdeFirestore() {
        db.collection("planes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mMap.clear(); // Limpia los marcadores anteriores

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String estado = doc.getString("estado");
                        com.google.firebase.Timestamp fechaHora = doc.getTimestamp("fechaHora");

                        if (fechaHora != null && "activo".equals(estado)) {
                            if (fechaHora.toDate().before(new java.util.Date())) {
                                doc.getReference().update("estado", "finalizado");
                                continue;
                            }
                        }

                        if ("cancelado".equals(estado) || "finalizado".equals(estado)) continue;

                        Double lat = doc.getDouble("latitud");
                        Double lng = doc.getDouble("longitud");
                        String nombre = doc.getString("nombre");

                        if (lat != null && lng != null && nombre != null) {
                            LatLng ubicacion = new LatLng(lat, lng);
                            Bitmap original = BitmapFactory.decodeResource(getResources(), R.drawable.marcador_verde);
                            Bitmap resized = Bitmap.createScaledBitmap(original, 100, 100, false);
                            mMap.addMarker(new MarkerOptions()
                                            .position(ubicacion)
                                            .title(nombre)
                                            .icon(BitmapDescriptorFactory.fromBitmap(resized)))
                                    .setTag(doc.getId());
                        }
                    }

                    // Escuchar clics en marcadores
                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        Marker lastClickedMarker = null;

                        @Override
                        public boolean onMarkerClick(@NonNull Marker marker) {
                            if (marker.equals(lastClickedMarker)) {
                                String planId = (String) marker.getTag();
                                if (planId != null) {
                                    abrirDetallesDelPlan(planId);
                                }
                                return true;
                            } else {
                                lastClickedMarker = marker;
                                marker.showInfoWindow();
                                return true;
                            }
                        }
                    });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error cargando planes", Toast.LENGTH_SHORT).show()
                );
    }



    /*private void buscarUbicacion(String ubicacion) {
        if (ubicacion == null || ubicacion.isEmpty()) return;

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> direcciones = geocoder.getFromLocationName(ubicacion, 1);
            if (direcciones != null && !direcciones.isEmpty()) {
                Address direccion = direcciones.get(0);
                LatLng posicion = new LatLng(direccion.getLatitude(), direccion.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posicion, 15));
            } else {
                Toast.makeText(this, "Ubicación no encontrada", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al buscar la ubicación", Toast.LENGTH_SHORT).show();
        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void abrirDetallesDelPlan(String planId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double userLat = location.getLatitude();
                double userLng = location.getLongitude();

                db.collection("planes").document(planId).get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double planLat = doc.getDouble("latitud");
                        Double planLng = doc.getDouble("longitud");

                        String distanciaStr = "";
                        if (planLat != null && planLng != null) {
                            float[] results = new float[1];
                            Location.distanceBetween(userLat, userLng, planLat, planLng, results);
                            float km = results[0] / 1000;
                            distanciaStr = String.format("%.2f km de ti", km);
                        }

                        Intent intent = new Intent(this, detallesPlan.class);
                        intent.putExtra("planId", planId);
                        intent.putExtra("nombre", doc.getString("nombre"));
                        intent.putExtra("categoria", doc.getString("categoria"));
                        intent.putExtra("descripcion", doc.getString("descripcion"));
                        intent.putExtra("direccion", doc.getString("direccion"));
                        intent.putExtra("distancia", distanciaStr);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "No se encontró el plan", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(this, "No se pudo obtener tu ubicación", Toast.LENGTH_SHORT).show();
            }
        });
    }



}
