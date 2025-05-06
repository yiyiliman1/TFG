package com.example.join.menu.chat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.join.R;

import java.util.List;

public class MensajeAdapter extends RecyclerView.Adapter<MensajeAdapter.MensajeViewHolder> {

    private List<Mensaje> mensajes;
    private String currentUserId;

    public MensajeAdapter(List<Mensaje> mensajes, String currentUserId) {
        this.mensajes = mensajes;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MensajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensaje, parent, false);
        return new MensajeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MensajeViewHolder holder, int position) {
        Mensaje mensaje = mensajes.get(position);
        holder.messageText.setText(mensaje.getText());

        // Alinear y cambiar fondo según el remitente
        if (mensaje.getSenderId().equals(currentUserId)) {
            holder.messageText.setBackgroundResource(R.drawable.message_bg_right);
            holder.container.setGravity(Gravity.END);
        } else {
            holder.messageText.setBackgroundResource(R.drawable.message_bg_left);
            holder.container.setGravity(Gravity.START);
        }
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    static class MensajeViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        LinearLayout container;

        public MensajeViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            container = itemView.findViewById(R.id.messageContainer);
        }
    }
}
