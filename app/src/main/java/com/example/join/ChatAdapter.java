package com.example.join;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.BaseAdapter;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_solicitud_amistad, parent, false);

            TextView textMensaje = convertView.findViewById(R.id.textSolicitudMensaje);
            Button btnAceptar = convertView.findViewById(R.id.botonAceptarAmistad);

            textMensaje.setText("Solicitud de amistad de " + mensaje.getAutorNombre());

            if (chatConfirmado) {
                btnAceptar.setVisibility(View.GONE);
            } else {
                btnAceptar.setVisibility(View.VISIBLE);
                btnAceptar.setOnClickListener(v -> {
                    aceptarSolicitud(mensaje.getAutorId());
                    btnAceptar.setVisibility(View.GONE); // Ocultarlo después de aceptar
                });
            }

            return convertView;
        }

        // Mensaje informativo
        if ("info".equals(tipo)) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_mensaje_info, parent, false);
            TextView texto = convertView.findViewById(R.id.textMensajeInfo);
            texto.setText(mensaje.getTexto());
            return convertView;
        }

        // Mensaje normal
        boolean esMio = mensaje.getAutorId().equals(currentUserId);
        int layoutId = esMio ? R.layout.item_mensaje_derecha : R.layout.item_mensaje_izquierda;

        convertView = LayoutInflater.from(context).inflate(layoutId, parent, false);

        TextView autor = convertView.findViewById(R.id.textAutor);
        TextView texto = convertView.findViewById(R.id.textMensaje);
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

        // Construir ID de chat
        String chatId = currentUserId.compareTo(otroId) < 0
                ? currentUserId + "_" + otroId
                : otroId + "_" + currentUserId;

        // Marcar el chat como confirmado
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

        // Actualizar el estado local
        this.chatConfirmado = true;
        notifyDataSetChanged(); // Refresca la lista para ocultar botón
    }
}
