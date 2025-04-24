package com.example.joinapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;
import java.util.Map;

public class DNI extends AppCompatActivity {

    private EditText dniEditText;
    private Button registerButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String correo, usuario, contrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dni);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        dniEditText = findViewById(R.id.editTextDni);
        registerButton = findViewById(R.id.buttonRegister);

        // Recuperar datos del intent
        correo = getIntent().getStringExtra("correo");
        usuario = getIntent().getStringExtra("usuario");
        contrasena = getIntent().getStringExtra("contrasena");

        registerButton.setOnClickListener(v -> {
            String dni = dniEditText.getText().toString().trim();

            if (!dni.isEmpty()) {
                registrarUsuario(correo, contrasena, usuario, dni);
            } else {
                Toast.makeText(this, "Por favor ingresa tu DNI/NIE", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registrarUsuario(String correo, String contrasena, String usuario, String dni) {
        mAuth.createUserWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Enviar correo de verificación
                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(verificationTask -> {
                                    if (verificationTask.isSuccessful()) {
                                        // Guardar los datos en Firestore
                                        String userId = mAuth.getCurrentUser().getUid();
                                        Map<String, Object> user = new HashMap<>();
                                        user.put("correo", correo);
                                        user.put("usuario", usuario);
                                        user.put("dni", dni);

                                        db.collection("usuarios").document(userId)
                                                .set(user)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(this, "Usuario registrado. Verifica tu correo antes de iniciar sesión.", Toast.LENGTH_LONG).show();

                                                    // Cerrar sesión para evitar que acceda sin verificar
                                                    mAuth.signOut();

                                                    // Enviar al login
                                                    Intent intent = new Intent(dniActivity.this, loginalex.class);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(this, "No se pudo enviar el correo de verificación", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        Toast.makeText(this, "Error al crear la cuenta: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    }
}