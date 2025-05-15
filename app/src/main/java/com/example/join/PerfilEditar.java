package com.example.join;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class PerfilEditar extends AppCompatActivity {

    private EditText editBiografia, editUbicacion;
    private TextView textInteres;
    private Switch switchHistorial, switchPrivado;
    private Button guardarBtn, btnCambiarFoto;
    private ImageView imageViewFoto;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String userId;
    private Uri imagenSeleccionadaUri;

    private static final int REQUEST_PERMISSION_CODE = 102;

    private final ActivityResultLauncher<Intent> someActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            imagenSeleccionadaUri = result.getData().getData();
                            imageViewFoto.setImageURI(imagenSeleccionadaUri); // Vista previa
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_editar);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Referencias UI
        editBiografia = findViewById(R.id.editTextText4);
        editUbicacion = findViewById(R.id.editTextText7);
        textInteres = findViewById(R.id.textView21);
        switchHistorial = findViewById(R.id.switch2);
        switchPrivado = findViewById(R.id.switch4);
        guardarBtn = findViewById(R.id.button4);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);
        imageViewFoto = findViewById(R.id.imageView29);

        cargarDatosUsuario();

        guardarBtn.setOnClickListener(v -> guardarDatos());
        btnCambiarFoto.setOnClickListener(v -> pedirPermisoYSeleccionarImagen());

        db.collection("usuarios").document(userId).get().addOnSuccessListener(doc -> {
            String url = doc.getString("fotoPerfil");
            if (url != null && !url.isEmpty()) {
                Glide.with(this).load(url).into(imageViewFoto);
            }
        });
    }

    private void pedirPermisoYSeleccionarImagen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    REQUEST_PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirGaleria();
        } else {
            Toast.makeText(this, "Permiso denegado para acceder a la galería", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        someActivityResultLauncher.launch(intent);
    }

    private void cargarDatosUsuario() {
        db.collection("usuarios").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        editBiografia.setText(doc.getString("biografia"));
                        editUbicacion.setText(doc.getString("ubicacion"));
                        textInteres.setText(doc.getString("intereses"));
                        switchHistorial.setChecked(Boolean.TRUE.equals(doc.getBoolean("historial")));
                        switchPrivado.setChecked(Boolean.TRUE.equals(doc.getBoolean("perfilPrivado")));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show());
    }

    private void guardarDatos() {
        String biografia = editBiografia.getText().toString().trim();
        String ubicacion = editUbicacion.getText().toString().trim();
        String intereses = textInteres.getText().toString().trim();
        boolean historial = switchHistorial.isChecked();
        boolean perfilPrivado = switchPrivado.isChecked();

        Map<String, Object> cambios = new HashMap<>();
        cambios.put("biografia", biografia);
        cambios.put("ubicacion", ubicacion);
        cambios.put("intereses", intereses);
        cambios.put("historial", historial);
        cambios.put("perfilPrivado", perfilPrivado);

        if (imagenSeleccionadaUri != null) {
            StorageReference ref = storage.getReference().child("usuarios").child(userId + ".jpg");
            Log.d("PerfilEditar", "URI seleccionada: " + imagenSeleccionadaUri);

            ref.putFile(imagenSeleccionadaUri)
                    .addOnSuccessListener(taskSnapshot ->
                            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                cambios.put("fotoPerfil", uri.toString());
                                guardarEnFirestore(cambios);
                            }))
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show());
        } else {
            guardarEnFirestore(cambios);
        }
    }

    private void guardarEnFirestore(Map<String, Object> cambios) {
        db.collection("usuarios").document(userId)
                .update(cambios)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar perfil", Toast.LENGTH_SHORT).show());
    }
}
