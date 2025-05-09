package com.example.join.sesion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.join.R;

public class Register extends AppCompatActivity {

    private EditText correoEditText, usuarioEditText, contrasenaEditText, repetirContrasenaEditText;
    private Button siguienteButton;
    private TextView iniciarSesionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa el TextView que tiene el texto "¡Inicia sesión!"
        iniciarSesionTextView = findViewById(R.id.textView4);  // Este es el id de tu TextView

        // Añade un OnClickListener al TextView
        iniciarSesionTextView.setOnClickListener(v -> {
            // Crea un Intent para abrir la actividad de login
            Intent intent = new Intent(Register.this, Login.class); // Asegúrate de que loginalex sea tu actividad de login
            startActivity(intent);
        });

        correoEditText = findViewById(R.id.editTextText5);
        usuarioEditText = findViewById(R.id.editTextTextEmailAddress2);
        contrasenaEditText = findViewById(R.id.editTextTextPassword2);
        repetirContrasenaEditText = findViewById(R.id.editTextTextPassword3);
        siguienteButton = findViewById(R.id.button);

        siguienteButton.setOnClickListener(v -> {
            String correo = correoEditText.getText().toString().trim();
            String usuario = usuarioEditText.getText().toString().trim();
            String contrasena = contrasenaEditText.getText().toString();
            String repetir = repetirContrasenaEditText.getText().toString();

            if (!correo.isEmpty() && !usuario.isEmpty() && !contrasena.isEmpty() && contrasena.equals(repetir)) {
                Intent intent = new Intent(Register.this, DNI.class);
                intent.putExtra("correo", correo);
                intent.putExtra("usuario", usuario);
                intent.putExtra("contrasena", contrasena);
                startActivity(intent);
            } else {
                // Manejar errores: campos vacíos o contraseñas distintas
            }
        });
    }
}