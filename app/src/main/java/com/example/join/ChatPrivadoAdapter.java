package com.example.join;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ChatPrivadoAdapter extends RecyclerView.Adapter<ChatPrivadoAdapter.ChatViewHolder> {

    private Context context;
    private List<ChatPrivadoModelo> listaChats;

    public ChatPrivadoAdapter(Context context, List<ChatPrivadoModelo> listaChats) {
        this.context = context;
        this.listaChats = listaChats;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_privado, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatPrivadoModelo chat = listaChats.get(position);
        holder.nombre.setText(chat.getNombre());

        if (chat.getFotoUrl() != null && !chat.getFotoUrl().isEmpty()) {
            Glide.with(context).load(chat.getFotoUrl()).into(holder.foto);
        } else {
            holder.foto.setImageResource(R.drawable.default_user);
        }

        View.OnClickListener listener = v -> {
            Intent intent = new Intent(context, ChatPrivado.class);
            intent.putExtra("chatId", chat.getChatId());
            intent.putExtra("usuarioId", chat.getUsuarioId());
            intent.putExtra("nombre", chat.getNombre());
            context.startActivity(intent);
        };

        holder.foto.setOnClickListener(listener);
        holder.nombre.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return listaChats.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView foto;
        TextView nombre;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            foto = itemView.findViewById(R.id.fotoUsuarioChat);
            nombre = itemView.findViewById(R.id.nombreUsuarioChat);
        }
    }
}
