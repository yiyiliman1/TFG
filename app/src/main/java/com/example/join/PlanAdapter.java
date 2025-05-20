package com.example.join;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private List<PlanItem> listaPlanes;
    private Context context;
    private double userLat, userLng;

    public PlanAdapter(List<PlanItem> listaPlanes, Context context, double userLat, double userLng) {
        this.listaPlanes = listaPlanes;
        this.context = context;
        this.userLat = userLat;
        this.userLng = userLng;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_plan_cercano, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        PlanItem plan = listaPlanes.get(position);

        holder.textTitulo.setText(plan.getNombre());
        holder.textTipo.setText(plan.getCategoria());

        // Calcular y mostrar distancia
        String distancia = calcularDistancia(plan.getLatitud(), plan.getLongitud()) + " km de ti";
        holder.textDistancia.setText(distancia);

        // Mostrar imagen del plan desde Firebase o imagen por defecto
        if (plan.getFotoUrl() != null && !plan.getFotoUrl().isEmpty()) {
            Glide.with(context).load(plan.getFotoUrl()).into(holder.imagePlan);
        } else {
            holder.imagePlan.setImageResource(R.drawable.personalogo);
        }

        // Mostrar fecha y hora si está disponible
        if (plan.getFechaHora() != null) {
            Date fecha = plan.getFechaHora();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy - HH:mm", new Locale("es", "ES"));
            holder.textFechaHora.setText(sdf.format(fecha));
        } else {
            holder.textFechaHora.setText("");
        }

        // Click para abrir detalles del plan
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, detallesPlan.class);
            intent.putExtra("nombre", plan.getNombre());
            intent.putExtra("categoria", plan.getCategoria());
            intent.putExtra("distancia", distancia);
            intent.putExtra("descripcion", plan.getDescripcion());
            intent.putExtra("direccion", plan.getDireccion());
            intent.putExtra("planId", plan.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaPlanes.size();
    }

    public static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo, textTipo, textDistancia, textFechaHora;
        ImageView imagePlan;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.textTitulo);
            textTipo = itemView.findViewById(R.id.textTipo);
            textDistancia = itemView.findViewById(R.id.textDistancia);
            textFechaHora = itemView.findViewById(R.id.textFechaHora);  // Este debe estar en el XML
            imagePlan = itemView.findViewById(R.id.imagePlan);
        }
    }

    private String calcularDistancia(double lat, double lng) {
        float[] results = new float[1];
        Location.distanceBetween(userLat, userLng, lat, lng, results);
        float distanceInMeters = results[0];
        float distanceInKm = distanceInMeters / 1000;
        return String.format("%.2f", distanceInKm);
    }
}
