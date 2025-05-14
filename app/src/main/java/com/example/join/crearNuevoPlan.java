package com.example.join;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class crearNuevoPlan extends AppCompatActivity {

    EditText nombreEditText, descripcionEditText, direccionEditText, fechaEditText, horaEditText;
    TextView categoriaTextView, participantesTextView;
    Switch soloAmigosSwitch;
    Button crearBtn;

    FirebaseFirestore db;

    String[] categorias = {"Cena", "Fiesta", "Deporte", "Cultura", "Excursión", "Videojuegos"};

    TextView btnSumar, btnRestar, txtParticipantes, btnSinLimite;
    int contadorParticipantes = 0;
    boolean sinLimite = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 100;
    private MapView mapView;
    private GoogleMap googleMap;
    private double userLat = 0.0;
    private double userLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_nuevo_plan);

        db = FirebaseFirestore.getInstance();

        // Vincular vistas
        nombreEditText = findViewById(R.id.editTextText);
        descripcionEditText = findViewById(R.id.editTextText2);
        direccionEditText = findViewById(R.id.editTextText3);
        fechaEditText = findViewById(R.id.editTextDate);
        horaEditText = findViewById(R.id.editTextText6);
        categoriaTextView = findViewById(R.id.textView21);
        participantesTextView = findViewById(R.id.textView24);
        soloAmigosSwitch = findViewById(R.id.switch1);
        crearBtn = findViewById(R.id.button3);

        mapView = findViewById(R.id.mapView2);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap gMap) {
                googleMap = gMap;
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Button usarUbicacionBtn = findViewById(R.id.button2);
        usarUbicacionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerUbicacionUsuario();
            }
        });



        crearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crearPlan();
            }
        });

        btnSumar = findViewById(R.id.textView25);       // "+"
        btnRestar = findViewById(R.id.textView23);      // "-"
        txtParticipantes = findViewById(R.id.textView24); // número
        btnSinLimite = findViewById(R.id.textView26);   // "Sin límite"

        btnSumar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sinLimite) {
                    contadorParticipantes++;
                    txtParticipantes.setText(String.valueOf(contadorParticipantes));
                }
            }
        });

        btnRestar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sinLimite && contadorParticipantes > 0) {
                    contadorParticipantes--;
                    txtParticipantes.setText(String.valueOf(contadorParticipantes));
                }
            }
        });

        btnSinLimite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sinLimite = !sinLimite;

                if (sinLimite) {
                    txtParticipantes.setText("∞");
                } else {
                    contadorParticipantes = 0;
                    txtParticipantes.setText(String.valueOf(contadorParticipantes));
                }
            }
        });


        categoriaTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoCategorias();
            }
        });

        Button buscarDireccionBtn = findViewById(R.id.buttonBuscarDireccion);
        EditText direccionEditText = findViewById(R.id.editTextText3);

        buscarDireccionBtn.setOnClickListener(v -> {
            String direccionTexto = direccionEditText.getText().toString().trim();
            if (!direccionTexto.isEmpty()) {
                buscarDireccion(direccionTexto);
            } else {
                Toast.makeText(this, "Escribe una dirección para buscar", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void crearPlan() {
        String nombre = nombreEditText.getText().toString().trim();
        String descripcion = descripcionEditText.getText().toString().trim();
        String direccion = direccionEditText.getText().toString().trim();
        String fecha = fechaEditText.getText().toString().trim();
        String hora = horaEditText.getText().toString().trim();
        String categoria = categoriaTextView.getText().toString().trim();

        // Validación de participantes
        if (!sinLimite && contadorParticipantes == 0) {
            Toast.makeText(this, "Debes permitir al menos un participante", Toast.LENGTH_SHORT).show();
            return;
        }

        int participantes = sinLimite ? -1 : contadorParticipantes;
        boolean soloAmigos = soloAmigosSwitch.isChecked();

        Plan nuevoPlan = new Plan(
                nombre,
                descripcion,
                direccion,
                fecha,
                hora,
                categoria,
                participantes,
                soloAmigos,
                userLat,
                userLng
        );


        db.collection("planes")
                .add(nuevoPlan)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Plan creado correctamente", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(crearNuevoPlan.this, menu.class);
                    intent.putExtra("mensaje_exito", "¡Plan creado correctamente!");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al crear el plan", Toast.LENGTH_SHORT).show());
    }


    private void mostrarDialogoCategorias() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Elige una categoría");
        builder.setItems(categorias, (dialog, which) -> {
            categoriaTextView.setText(categorias[which]);
        });
        builder.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacionUsuario();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void obtenerUbicacionUsuario() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION
            );
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                userLat = location.getLatitude();
                userLng = location.getLongitude();

                if (googleMap != null) {
                    LatLng miUbicacion = new LatLng(userLat, userLng);
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(miUbicacion).title("Tu ubicación"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 15));
                }
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void buscarDireccion(String direccion) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> direcciones = geocoder.getFromLocationName(direccion, 1);
            if (direcciones != null && !direcciones.isEmpty()) {
                Address direccionEncontrada = direcciones.get(0);
                userLat = direccionEncontrada.getLatitude();
                userLng = direccionEncontrada.getLongitude();

                LatLng posicion = new LatLng(userLat, userLng);
                if (googleMap != null) {
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(posicion).title("Ubicación encontrada"));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 15));
                }

                Toast.makeText(this, "Dirección encontrada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se pudo encontrar esa dirección", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al buscar dirección", Toast.LENGTH_SHORT).show();
        }
    }




}
