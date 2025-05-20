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
    private int layoutId;
    private boolean clickActivo;

    public PlanAdapter(List<PlanItem> listaPlanes, Context context, double userLat, double userLng, int layoutId, boolean clickActivo) {
        this.listaPlanes = listaPlanes;
        this.context = context;
        this.userLat = userLat;
        this.userLng = userLng;
        this.layoutId = layoutId;
        this.clickActivo = clickActivo;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        PlanItem plan = listaPlanes.get(position);

        if (holder.textTitulo != null) {
            holder.textTitulo.setText(plan.getNombre());
        }

        if (holder.textTipo != null) {
            holder.textTipo.setText(plan.getCategoria());
        }

        String distancia = calcularDistancia(plan.getLatitud(), plan.getLongitud()) + " km de ti";

        if (holder.textFechaHora != null && plan.getFechaHora() != null) {
            Date fecha = plan.getFechaHora();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy - HH:mm", new Locale("es", "ES"));
            holder.textFechaHora.setText(sdf.format(fecha));
        }

        if (holder.textDistancia != null) {
            holder.textDistancia.setText(distancia);
        }

        if (holder.textEstado != null && plan.getEstado() != null) {
            holder.textEstado.setText("Estado: " + plan.getEstado());
        }

        if (holder.imagePlan != null) {
            if (plan.getFotoUrl() != null && !plan.getFotoUrl().isEmpty()) {
                Glide.with(context).load(plan.getFotoUrl()).into(holder.imagePlan);
            } else {
                holder.imagePlan.setImageResource(R.drawable.personalogo);
            }
        }

        if (clickActivo) {
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, detallesPlan.class);
                intent.putExtra("nombre", plan.getNombre());
                intent.putExtra("categoria", plan.getCategoria());
                intent.putExtra("descripcion", plan.getDescripcion());
                intent.putExtra("direccion", plan.getDireccion());
                intent.putExtra("planId", plan.getId());
                intent.putExtra("distancia", distancia);
                context.startActivity(intent);
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return listaPlanes.size();
    }

    public class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo, textTipo, textDistancia, textFechaHora, textEstado;
        ImageView imagePlan;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = safeFindText(itemView, R.id.textTitulo, R.id.nombrePlan);
            textTipo = safeFindText(itemView, R.id.textTipo);
            textDistancia = safeFindText(itemView, R.id.textDistancia);
            textFechaHora = safeFindText(itemView, R.id.textFechaHora, R.id.fechaPlan);
            textEstado = safeFindText(itemView, R.id.textEstado);
            imagePlan = itemView.findViewById(R.id.imagePlan);
        }

        private TextView safeFindText(View itemView, int... ids) {
            for (int id : ids) {
                TextView t = itemView.findViewById(id);
                if (t != null) return t;
            }
            return null;
        }
    }

    private String calcularDistancia(double lat, double lng) {
        float[] results = new float[1];
        Location.distanceBetween(userLat, userLng, lat, lng, results);
        float distanceInKm = results[0] / 1000f;
        return String.format("%.2f", distanceInKm);
    }
}
