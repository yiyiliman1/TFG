package com.example.join;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class miPerfil extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView textUsuario, textCorreo, textIntereses;
    private EditText editBiografia, editUbicacion;
    private Switch switchPrivado;
    private Button btnEditar, btnCerrar;
    private ImageView imageViewFoto; // Agrega esto arriba en tu clase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_perfil);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Referencias UI
        textUsuario = findViewById(R.id.textView30);
        textCorreo = findViewById(R.id.textView31);
        editBiografia = findViewById(R.id.editTextText4);
        editUbicacion = findViewById(R.id.editTextText7);
        textIntereses = findViewById(R.id.textView21);
        switchPrivado = findViewById(R.id.switch4);
        btnEditar = findViewById(R.id.button5);
        btnCerrar = findViewById(R.id.button4);
        imageViewFoto = findViewById(R.id.imageView29); // AÑADIDO

        // Desactivar edición
        editBiografia.setEnabled(false);
        editUbicacion.setEnabled(false);
        switchPrivado.setEnabled(false);

        cargarDatosUsuario();

        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(miPerfil.this, PerfilEditar.class);
            startActivity(intent);
        });

        btnCerrar.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(miPerfil.this, login.class));
            finish();
        });

        // Botón back arriba a la izquierda
        ImageView back = findViewById(R.id.imageView);
        back.setOnClickListener(v -> finish());
    }

    private void cargarDatosUsuario() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "No hay usuario activo", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Mostrar correo directamente
        textCorreo.setText(mAuth.getCurrentUser().getEmail());

        db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        textUsuario.setText(doc.getString("usuario"));
                        editBiografia.setText(doc.getString("biografia"));
                        editUbicacion.setText(doc.getString("ubicacion"));
                        textIntereses.setText(doc.getString("intereses"));
                        switchPrivado.setChecked(Boolean.TRUE.equals(doc.getBoolean("perfilPrivado")));
                        String url = doc.getString("fotoPerfil");
                        if (url != null && !url.isEmpty()) {
                            Glide.with(this).load(url).into(imageViewFoto);
                        }

                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error cargando perfil", Toast.LENGTH_SHORT).show());
    }
}
