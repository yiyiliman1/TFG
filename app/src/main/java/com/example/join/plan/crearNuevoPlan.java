package com.example.join.plan;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;

import android.Manifest;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.join.R;
import com.example.join.chats.BuscarUsuario;
import com.example.join.perfil.miPerfil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class crearNuevoPlan extends AppCompatActivity {

    EditText nombreEditText, descripcionEditText, direccionEditText, fechaEditText, horaEditText;
    TextView categoriaTextView, participantesTextView;
    Switch soloAmigosSwitch;
    Button crearBtn;

    FirebaseFirestore db;
    StorageReference storageReference;

    String[] categorias = {"Cena", "Fiesta", "Deporte", "Cultura", "Excursión", "Videojuegos"};

    TextView btnSumar, btnRestar, txtParticipantes, btnSinLimite;
    int contadorParticipantes = 0;
    boolean sinLimite = false;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 100;
    private Uri selectedImageUri;
    private Uri defaultImageUri;
    private ImageView previewImagenPlan;
    private MapView mapView;
    private GoogleMap googleMap;
    private double userLat = 0.0;
    private double userLng = 0.0;

    Calendar fechaHoraSeleccionada = Calendar.getInstance();

    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_nuevo_plan);

        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("fotos_planes");

        nombreEditText = findViewById(R.id.editTextText);
        descripcionEditText = findViewById(R.id.editTextText2);
        direccionEditText = findViewById(R.id.editTextText3);
        fechaEditText = findViewById(R.id.editTextDate);
        horaEditText = findViewById(R.id.editTextText6);
        categoriaTextView = findViewById(R.id.textView21);
        participantesTextView = findViewById(R.id.textView24);
        soloAmigosSwitch = findViewById(R.id.switch1);
        crearBtn = findViewById(R.id.button3);
        previewImagenPlan = findViewById(R.id.previewImagenPlan);

        defaultImageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.personalogo);
        previewImagenPlan.setImageURI(defaultImageUri);
        previewImagenPlan.setVisibility(View.VISIBLE);

        fechaEditText.setFocusable(false);
        horaEditText.setFocusable(false);

        mapView = findViewById(R.id.mapView2);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(gMap -> googleMap = gMap);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        previewImagenPlan.setImageURI(selectedImageUri);
                        previewImagenPlan.setVisibility(View.VISIBLE);
                        Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show();
                    }
                });

        ImageView botonMapa = findViewById(R.id.imageView4);
        botonMapa.setOnClickListener(v -> {
            Intent intent = new Intent(crearNuevoPlan.this, menu.class);
            startActivity(intent);
        });
        ImageView botonPerfil = findViewById(R.id.imageView8);
        botonPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(crearNuevoPlan.this, miPerfil.class);
            startActivity(intent);
        });
        ImageView botonChat = findViewById(R.id.imageView2);
        botonChat.setOnClickListener(v -> {
            Intent intent = new Intent(crearNuevoPlan.this, BuscarUsuario.class);
            startActivity(intent);
        });
        ImageView botonListas = findViewById(R.id.imageView10);
        botonListas.setOnClickListener(v -> {
            Intent intent = new Intent(crearNuevoPlan.this, listarPlanesCercanos.class);
            startActivity(intent);
        });
        ImageView botonPlan = findViewById(R.id.imageView7);
        botonPlan.setOnClickListener(v -> {
            startActivity(new Intent(this, crearNuevoPlan.class));
        });

        Button usarUbicacionBtn = findViewById(R.id.button2);
        usarUbicacionBtn.setOnClickListener(v -> obtenerUbicacionUsuario());

        Button btnSubirFoto = findViewById(R.id.btnSubirFoto);
        btnSubirFoto.setOnClickListener(v -> mostrarOpcionesImagen());

        crearBtn.setOnClickListener(v -> crearPlan());

        btnSumar = findViewById(R.id.textView25);
        btnRestar = findViewById(R.id.textView23);
        txtParticipantes = findViewById(R.id.textView24);
        btnSinLimite = findViewById(R.id.textView26);

        btnSumar.setOnClickListener(v -> {
            if (!sinLimite) {
                contadorParticipantes++;
                txtParticipantes.setText(String.valueOf(contadorParticipantes));
            }
        });

        btnRestar.setOnClickListener(v -> {
            if (!sinLimite && contadorParticipantes > 0) {
                contadorParticipantes--;
                txtParticipantes.setText(String.valueOf(contadorParticipantes));
            }
        });

        btnSinLimite.setOnClickListener(v -> {
            sinLimite = !sinLimite;
            txtParticipantes.setText(sinLimite ? "∞" : String.valueOf(0));
            contadorParticipantes = sinLimite ? contadorParticipantes : 0;
        });

        categoriaTextView.setOnClickListener(v -> mostrarDialogoCategorias());

        Button buscarDireccionBtn = findViewById(R.id.buttonBuscarDireccion);
        buscarDireccionBtn.setOnClickListener(v -> {
            String direccionTexto = direccionEditText.getText().toString().trim();
            if (!direccionTexto.isEmpty()) {
                buscarDireccion(direccionTexto);
            } else {
                Toast.makeText(this, "Escribe una dirección para buscar", Toast.LENGTH_SHORT).show();
            }
        });

        fechaEditText.setOnClickListener(v -> mostrarDatePicker());
        horaEditText.setOnClickListener(v -> mostrarTimePicker());
    }

    private void mostrarOpcionesImagen() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void crearPlan() {
        String nombre = nombreEditText.getText().toString().trim();
        String descripcion = descripcionEditText.getText().toString().trim();
        String direccion = direccionEditText.getText().toString().trim();
        String categoria = categoriaTextView.getText().toString().trim();
        String creadorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (!sinLimite && contadorParticipantes == 0) {
            Toast.makeText(this, "Debes permitir al menos un participante", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fechaEditText.getText().toString().isEmpty() || horaEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Selecciona una fecha y una hora", Toast.LENGTH_SHORT).show();
            return;
        }

        Timestamp fechaHoraTimestamp = new Timestamp(fechaHoraSeleccionada.getTime());
        Calendar ahora = Calendar.getInstance();
        if (fechaHoraSeleccionada.before(ahora)) {
            Toast.makeText(this, "No puedes crear un plan en el pasado", Toast.LENGTH_SHORT).show();
            return;
        }

        int limiteParticipantes = sinLimite ? -1 : contadorParticipantes;
        boolean soloAmigos = soloAmigosSwitch.isChecked();

        Map<String, Object> plan = new HashMap<>();
        plan.put("creadorId", creadorId);
        plan.put("nombre", nombre);
        plan.put("descripcion", descripcion);
        plan.put("direccion", direccion);
        plan.put("categoria", categoria);
        plan.put("limiteParticipantes", limiteParticipantes);
        plan.put("soloAmigos", soloAmigos);
        plan.put("latitud", userLat);
        plan.put("longitud", userLng);
        plan.put("participantes", new ArrayList<String>());
        plan.put("fechaHora", fechaHoraTimestamp);
        plan.put("estado", "activo");

        Uri imagenParaSubir = (selectedImageUri != null) ? selectedImageUri : defaultImageUri;

        try {
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagenParaSubir);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 800, 800, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] imageData = baos.toByteArray();

            StorageReference fotoRef = storageReference.child(UUID.randomUUID().toString() + ".jpg");
            fotoRef.putBytes(imageData)
                    .addOnSuccessListener(taskSnapshot -> fotoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        plan.put("fotoUrl", uri.toString());
                        guardarPlanEnFirestore(plan);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show());

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "No se pudo procesar la imagen", Toast.LENGTH_SHORT).show();
        }
    }


    private void guardarPlanEnFirestore(Map<String, Object> plan) {
        db.collection("planes")
                .add(plan)
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Elige una categoría");
        builder.setItems(categorias, (dialog, which) -> categoriaTextView.setText(categorias[which]));
        builder.show();
    }

    private void mostrarDatePicker() {
        final Calendar c = Calendar.getInstance();
        int año = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH);
        int dia = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            fechaHoraSeleccionada.set(Calendar.YEAR, year);
            fechaHoraSeleccionada.set(Calendar.MONTH, month);
            fechaHoraSeleccionada.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            fechaEditText.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year));
        }, año, mes, dia);
        datePicker.show();
    }

    private void mostrarTimePicker() {
        final Calendar c = Calendar.getInstance();
        int hora = c.get(Calendar.HOUR_OF_DAY);
        int minuto = c.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            fechaHoraSeleccionada.set(Calendar.HOUR_OF_DAY, hourOfDay);
            fechaHoraSeleccionada.set(Calendar.MINUTE, minute1);
            horaEditText.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1));
        }, hora, minuto, true);
        timePicker.show();
    }

    private void obtenerUbicacionUsuario() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_LOCATION_PERMISSION);
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

                // Obtener dirección a partir de lat/lng
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> direcciones = geocoder.getFromLocation(userLat, userLng, 1);
                    if (direcciones != null && !direcciones.isEmpty()) {
                        Address direccionEncontrada = direcciones.get(0);
                        String direccionTexto = direccionEncontrada.getAddressLine(0);
                        direccionEditText.setText(direccionTexto);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "No se pudo obtener la dirección exacta", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
            }
        });
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


                String direccionCompleta = direccionEncontrada.getAddressLine(0);
                direccionEditText.setText(direccionCompleta);

                Toast.makeText(this, "Dirección encontrada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se pudo encontrar esa dirección", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al buscar dirección", Toast.LENGTH_SHORT).show();
        }
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

    public void irAOtraPantalla(View view) {
        Intent intent = new Intent(this, menu.class);
        startActivity(intent);
    }


}
