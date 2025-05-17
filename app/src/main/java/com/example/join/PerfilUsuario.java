package com.example.join;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PerfilUsuario extends AppCompatActivity {

    TextView textUsuario, textCorreo, textBiografia, textUbicacion, textCategoria1, textCategoria2, textCategoria3;
    ImageView imageViewFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);

        textUsuario = findViewById(R.id.textView30);
        textCorreo = findViewById(R.id.textView31);
        textBiografia = findViewById(R.id.editTextText4);
        textUbicacion = findViewById(R.id.editTextText7);
        imageViewFoto = findViewById(R.id.imageView29);
        textCategoria1 = findViewById(R.id.textCategoria1);
        textCategoria2 = findViewById(R.id.textCategoria2);
        textCategoria3 = findViewById(R.id.textCategoria3);

        String userId = getIntent().getStringExtra("usuarioId");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Usuario no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseFirestore.getInstance().collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        textUsuario.setText(doc.getString("usuario"));
                        textCorreo.setText(doc.getString("email"));
                        textBiografia.setText(doc.getString("biografia"));
                        textUbicacion.setText(doc.getString("ubicacion"));

                        String url = doc.getString("fotoPerfil");
                        if (url != null && !url.isEmpty()) {
                            Glide.with(this).load(url).circleCrop().into(imageViewFoto);
                        }

                        Object interesesObj = doc.get("intereses");
                        if (interesesObj instanceof List) {
                            List<String> intereses = (List<String>) interesesObj;

                            textCategoria1.setText(intereses.size() > 0 ? intereses.get(0) : "");
                            textCategoria2.setText(intereses.size() > 1 ? intereses.get(1) : "");
                            textCategoria3.setText(intereses.size() > 2 ? intereses.get(2) : "");
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show());
    }
}
