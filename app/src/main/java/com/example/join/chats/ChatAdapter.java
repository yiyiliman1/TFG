package com.example.join.chats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.BaseAdapter;

import com.example.join.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class ChatAdapter extends BaseAdapter {
    private Context context;
    private List<MensajeChat> mensajes;
    private String currentUserId;

    private boolean chatConfirmado = false;

    public ChatAdapter(Context context, List<MensajeChat> mensajes) {
        this.context = context;
        this.mensajes = mensajes;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public int getCount() {
        return mensajes.size();
    }

    @Override
    public Object getItem(int position) {
        return mensajes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private String formatTime(Timestamp timestamp) {
        if (timestamp == null) return "";
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate());
    }

    public void setChatConfirmado(boolean confirmado) {
        this.chatConfirmado = confirmado;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MensajeChat mensaje = mensajes.get(position);
        String tipo = mensaje.getTipo() != null ? mensaje.getTipo() : "";

        // Solicitud de amistad recibida
        if ("solicitud_amistad".equals(tipo) && !mensaje.getAutorId().equals(currentUserId)) {
            convertView = LayoutInflater.from(context).inflate(com.example.join.R.layout.item_solicitud_amistad, parent, false);

            TextView textMensaje = convertView.findViewById(com.example.join.R.id.textSolicitudMensaje);
            Button btnAceptar = convertView.findViewById(com.example.join.R.id.botonAceptarAmistad);
            Button btnRechazar = convertView.findViewById(com.example.join.R.id.botonRechazarAmistad);

            textMensaje.setText("Solicitud de amistad de " + mensaje.getAutorNombre());

            if (chatConfirmado) {
                btnAceptar.setVisibility(View.GONE);
                btnRechazar.setVisibility(View.GONE);
            } else {
                btnAceptar.setVisibility(View.VISIBLE);
                btnRechazar.setVisibility(View.VISIBLE);

                btnAceptar.setOnClickListener(v -> {
                    aceptarSolicitud(mensaje.getAutorId());
                    btnAceptar.setVisibility(View.GONE);
                    btnRechazar.setVisibility(View.GONE);
                });

                btnRechazar.setOnClickListener(v -> {
                    rechazarSolicitud(mensaje.getAutorId());
                    btnAceptar.setVisibility(View.GONE);
                    btnRechazar.setVisibility(View.GONE);
                });
            }

            return convertView;
        }

        // Mensaje informativo
        if ("info".equals(tipo)) {
            convertView = LayoutInflater.from(context).inflate(com.example.join.R.layout.item_mensaje_info, parent, false);
            TextView texto = convertView.findViewById(com.example.join.R.id.textMensajeInfo);
            texto.setText(mensaje.getTexto());
            return convertView;
        }

        // Mensaje normal
        boolean esMio = mensaje.getAutorId().equals(currentUserId);
        int layoutId = esMio ? com.example.join.R.layout.item_mensaje_derecha : com.example.join.R.layout.item_mensaje_izquierda;

        convertView = LayoutInflater.from(context).inflate(layoutId, parent, false);

        TextView autor = convertView.findViewById(com.example.join.R.id.textAutor);
        TextView texto = convertView.findViewById(com.example.join.R.id.textMensaje);
        TextView hora = convertView.findViewById(R.id.textHora);

        autor.setText(mensaje.getAutorNombre());
        texto.setText(mensaje.getTexto());
        hora.setText(formatTime(mensaje.getTimestamp()));

        return convertView;
    }

    private void aceptarSolicitud(String otroId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Agregar cada uno a la lista de amigos del otro
        db.collection("usuarios").document(currentUserId)
                .update("amigos", FieldValue.arrayUnion(otroId));

        db.collection("usuarios").document(otroId)
                .update("amigos", FieldValue.arrayUnion(currentUserId));


        String chatId = currentUserId.compareTo(otroId) < 0
                ? currentUserId + "_" + otroId
                : otroId + "_" + currentUserId;


        db.collection("chats").document(chatId)
                .update("confirmado", true);

        // Enviar mensaje de confirmación al chat
        Map<String, Object> msg = new HashMap<>();
        msg.put("texto", "¡Ahora sois amigos!");
        msg.put("autorId", currentUserId);
        msg.put("autorNombre", "Sistema");
        msg.put("timestamp", Timestamp.now());
        msg.put("tipo", "info");

        db.collection("chats").document(chatId)
                .collection("mensajes")
                .add(msg);


        this.chatConfirmado = true;
        notifyDataSetChanged();
    }

    private void rechazarSolicitud(String otroId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        String chatId = currentUserId.compareTo(otroId) < 0
                ? currentUserId + "_" + otroId
                : otroId + "_" + currentUserId;

        // Fecha 7 días a futuro
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 7);
        Timestamp rechazadoHasta = new Timestamp(cal.getTime());

        // Guardar marca de rechazo
        db.collection("rechazos").document(chatId)
                .set(Collections.singletonMap("rechazadoHasta", rechazadoHasta));

        // Borrar mensajes del chat
        db.collection("chats").document(chatId).collection("mensajes")
                .get().addOnSuccessListener(query -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnSuccessListener(v -> {
                        // Borrar el documento del chat
                        db.collection("chats").document(chatId).delete();
                    });
                });
    }
}
