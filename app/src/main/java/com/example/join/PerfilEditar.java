package com.example.join;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PerfilEditar extends AppCompatActivity {

    private final String[] todasCategorias = {"Ocio", "Deportes", "Cine", "Música", "Lectura", "Tecnología", "Viajes"};
    private final boolean[] seleccionadas = new boolean[todasCategorias.length];
    private final java.util.List<String> categoriasElegidas = new java.util.ArrayList<>();


    private EditText editBiografia, editUbicacion;
    private TextView textInteres;
    private Switch switchHistorial, switchPrivado;
    private Button guardarBtn, btnCambiarFoto;
    private ImageView imageViewFoto;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String userId;
    private Uri imagenSeleccionadaUri;

    private EditText editUsuario;
    private TextView textCorreo;


    private static final int REQUEST_PERMISSION_CODE = 102;

    private final ActivityResultLauncher<Intent> someActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            imagenSeleccionadaUri = result.getData().getData();
                            Glide.with(this).load(imagenSeleccionadaUri).circleCrop().into(imageViewFoto);
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

        editUsuario = findViewById(R.id.textView30);
        textCorreo = findViewById(R.id.textView31);





        textInteres.setOnClickListener(v -> mostrarSelectorCategorias());
        cargarDatosUsuario();

        guardarBtn.setOnClickListener(v -> guardarDatos());
        btnCambiarFoto.setOnClickListener(v -> pedirPermisoYSeleccionarImagen());

        db.collection("usuarios").document(userId).get().addOnSuccessListener(doc -> {
            String url = doc.getString("fotoPerfil");
            if (url != null && !url.isEmpty()) {
                Glide.with(this).load(url).circleCrop().into(imageViewFoto);
            }
        });

        ImageView botonPlan = findViewById(R.id.imageView7);
        botonPlan.setOnClickListener(v -> {
            startActivity(new Intent(this, crearNuevoPlan.class));
        });

        ImageView botonChat = findViewById(R.id.imageView2);
        botonChat.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilEditar.this, BuscarUsuario.class);
            startActivity(intent);
        });

        ImageView botonMenu = findViewById(R.id.imageView4);
        botonMenu.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilEditar.this, menu.class);
            startActivity(intent);
        });

        ImageView botonPerfil = findViewById(R.id.imageView10);
        botonPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilEditar.this, listarPlanesCercanos.class);
            startActivity(intent);
        });

        ImageView botonPerfilAPerfil = findViewById(R.id.imageView8);
        botonPerfilAPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilEditar.this, miPerfil.class);
            startActivity(intent);
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
                        editUsuario.setText(doc.getString("usuario"));
                        textCorreo.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

                        editBiografia.setText(doc.getString("biografia"));
                        editUbicacion.setText(doc.getString("ubicacion"));
                        Object interesesObj = doc.get("intereses");
                        if (interesesObj instanceof java.util.List) {
                            java.util.List<String> interesesList = (java.util.List<String>) interesesObj;
                            categoriasElegidas.clear();
                            categoriasElegidas.addAll(interesesList);
                            // Resetear seleccionadas[]
                            for (int i = 0; i < todasCategorias.length; i++) {
                                seleccionadas[i] = interesesList.contains(todasCategorias[i]);
                            }

                            textInteres.setText(String.join(", ", interesesList));
                        } else {
                            textInteres.setText("");
                        }

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
        String usuario = editUsuario.getText().toString().trim();

        Map<String, Object> cambios = new HashMap<>();
        cambios.put("biografia", biografia);
        cambios.put("ubicacion", ubicacion);
        cambios.put("intereses", new ArrayList<>(categoriasElegidas));
        cambios.put("historial", historial);
        cambios.put("perfilPrivado", perfilPrivado);
        cambios.put("usuario", usuario);


        if (imagenSeleccionadaUri != null) {
            byte[] imagenComprimida = comprimirImagen(imagenSeleccionadaUri);
            if (imagenComprimida != null) {
                StorageReference ref = storage.getReference().child("usuarios").child(userId + ".jpg");
                ref.putBytes(imagenComprimida)
                        .addOnSuccessListener(taskSnapshot ->
                                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                    cambios.put("fotoPerfil", uri.toString());
                                    guardarEnFirestore(cambios);
                                }))
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            }
        } else {
            guardarEnFirestore(cambios);
        }
    }

    private void guardarEnFirestore(Map<String, Object> cambios) {
        db.collection("usuarios").document(userId)
                .update(cambios)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(PerfilEditar.this, miPerfil.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // Animación
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar perfil", Toast.LENGTH_SHORT).show());
    }



    private byte[] comprimirImagen(Uri uri) {
        try {
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 500, 500, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void mostrarSelectorCategorias() {

        // Reset antes de mostrar diálogo
        categoriasElegidas.clear();
        for (int i = 0; i < todasCategorias.length; i++) {
            if (seleccionadas[i]) {
                categoriasElegidas.add(todasCategorias[i]);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona hasta 3 categorías");

        builder.setMultiChoiceItems(todasCategorias, seleccionadas, (dialog, index, isChecked) -> {
            if (isChecked) {
                if (categoriasElegidas.size() < 3) {
                    categoriasElegidas.add(todasCategorias[index]);
                } else {
                    ((AlertDialog) dialog).getListView().setItemChecked(index, false);
                    Toast.makeText(this, "Solo puedes elegir 3 categorías", Toast.LENGTH_SHORT).show();
                }
            } else {
                categoriasElegidas.remove(todasCategorias[index]);
            }
        });

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            String seleccion = String.join(", ", categoriasElegidas);
            textInteres.setText(seleccion);
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    public void irAOtraPantalla(View view) {
        Intent intent = new Intent(this, menu.class);
        startActivity(intent);
    }
}
