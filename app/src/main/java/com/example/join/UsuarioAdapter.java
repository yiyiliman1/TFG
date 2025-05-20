package com.example.join;

import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private List<UsuarioModelo> listaUsuarios;
    private Context context;
    private int layoutId;

    public UsuarioAdapter(Context context, List<UsuarioModelo> listaUsuarios, int layoutId) {
        this.context = context;
        this.listaUsuarios = listaUsuarios;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        UsuarioModelo usuario = listaUsuarios.get(position);
        holder.nombre.setText(usuario.nombre != null ? usuario.nombre : "Sin nombre");

        if (usuario.fotoUrl != null && !usuario.fotoUrl.isEmpty()) {
            Glide.with(context).load(usuario.fotoUrl).circleCrop().into(holder.foto);
        } else {
            holder.foto.setImageResource(R.drawable.default_user);
        }

        View.OnClickListener irPerfil = v -> {
            Intent intent = new Intent(context, PerfilUsuario.class);
            intent.putExtra("usuarioId", usuario.id);
            context.startActivity(intent);
        };

        holder.foto.setOnClickListener(irPerfil);
        holder.nombre.setOnClickListener(irPerfil);
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        ImageView foto;
        TextView nombre;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            foto = itemView.findViewById(R.id.fotoUsuario);
            nombre = itemView.findViewById(R.id.nombreUsuario);
        }
    }
}
