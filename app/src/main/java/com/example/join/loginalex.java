package com.example.join;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class loginalex extends AppCompatActivity {

    private EditText correoEditText, contrasenaEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private TextView registroTextView;
    private CheckBox rememberMeCheckBox;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "prefs";
    private static final String REMEMBER = "remember";
    private static final String LOGGED_IN = "loggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.loginalex);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginalex), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();

        // Verificar si el usuario ya está logueado y tiene su correo verificado
        if (isUserLoggedIn()) {
            // Si ya está logueado y verificado, redirigir a la pantalla principal
            startActivity(new Intent(loginalex.this, menu.class));
            finish();
        }

        // Enlazar las vistas
        correoEditText = findViewById(R.id.editTextText5);
        contrasenaEditText = findViewById(R.id.editTextTextEmailAddress2);
        loginButton = findViewById(R.id.button);
        rememberMeCheckBox = findViewById(R.id.checkBox);
        registroTextView = findViewById(R.id.textView4);

        // Configurar el registro de nuevos usuarios
        registroTextView.setOnClickListener(v -> {
            Intent intent = new Intent(loginalex.this, MainActivity.class);
            startActivity(intent);
        });

        // Evento para el botón de login
        loginButton.setOnClickListener(v -> {
            String correo = correoEditText.getText().toString().trim();
            String contrasena = contrasenaEditText.getText().toString().trim();

            if (!correo.isEmpty() && !contrasena.isEmpty()) {
                loginUser(correo, contrasena);
            } else {
                Toast.makeText(this, "Por favor ingresa el correo y la contraseña", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String correo, String contrasena) {
        mAuth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Guardar preferencia si se marcó "Recuérdame"
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(REMEMBER, rememberMeCheckBox.isChecked());
                            editor.putBoolean(LOGGED_IN, true);
                            editor.apply();

                            // Redirigir al menú
                            Intent intent = new Intent(loginalex.this, menu.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Si el email no está verificado
                            Toast.makeText(loginalex.this, "Verifica tu correo electrónico antes de continuar", Toast.LENGTH_LONG).show();
                            mAuth.signOut();  // Cerrar sesión si no está verificado
                        }
                    } else {
                        Toast.makeText(loginalex.this, "Error al iniciar sesión: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método para verificar si el usuario ya está logueado
    private boolean isUserLoggedIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        boolean remember = sharedPreferences.getBoolean(REMEMBER, false);
        boolean loggedIn = sharedPreferences.getBoolean(LOGGED_IN, false);

        // Si el usuario está logueado, y tiene preferencia "recuérdame" activada, y el correo está verificado
        return remember && loggedIn && user != null && user.isEmailVerified();
    }
}
