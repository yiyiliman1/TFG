package com.example.join;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.BaseAdapter;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends BaseAdapter {
    private Context context;
    private List<MensajeChat> mensajes;
    private String currentUserId;

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

    private String formatTime(com.google.firebase.Timestamp timestamp) {
        if (timestamp == null) return "";
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp.toDate());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MensajeChat mensaje = mensajes.get(position);

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
}
