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

    public PlanAdapter(List<PlanItem> listaPlanes, Context context, double userLat, double userLng, int layoutId) {
        this.listaPlanes = listaPlanes;
        this.context = context;
        this.userLat = userLat;
        this.userLng = userLng;
        this.layoutId = layoutId;
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

        // Título
        if (holder.textTitulo != null) {
            holder.textTitulo.setText(plan.getNombre());
        }

        // Categoría
        if (holder.textTipo != null) {
            holder.textTipo.setText(plan.getCategoria());
        }

        // Fecha y hora
        if (holder.textFechaHora != null && plan.getFechaHora() != null) {
            Date fecha = plan.getFechaHora();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy - HH:mm", new Locale("es", "ES"));
            holder.textFechaHora.setText(sdf.format(fecha));
        }

        // Distancia
        if (holder.textDistancia != null) {
            String distancia = calcularDistancia(plan.getLatitud(), plan.getLongitud()) + " km de ti";
            holder.textDistancia.setText(distancia);
        }

        // Imagen
        if (holder.imagePlan != null) {
            if (plan.getFotoUrl() != null && !plan.getFotoUrl().isEmpty()) {
                Glide.with(context).load(plan.getFotoUrl()).into(holder.imagePlan);
            } else {
                holder.imagePlan.setImageResource(R.drawable.personalogo);
            }
        }

        // Click a detalles
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, detallesPlan.class);
            intent.putExtra("nombre", plan.getNombre());
            intent.putExtra("categoria", plan.getCategoria());
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

    public class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo, textTipo, textDistancia, textFechaHora;
        ImageView imagePlan;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);

            // Intenta cargar según distintos IDs posibles
            textTitulo = safeFindText(itemView, R.id.textTitulo, R.id.nombrePlan);
            textTipo = safeFindText(itemView, R.id.textTipo);
            textDistancia = safeFindText(itemView, R.id.textDistancia);
            textFechaHora = safeFindText(itemView, R.id.textFechaHora, R.id.fechaPlan);
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
