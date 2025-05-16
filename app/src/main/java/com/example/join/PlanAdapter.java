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

import java.util.List;

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

        // Calculamos distancia aquí
        String distancia = calcularDistancia(plan.getLatitud(), plan.getLongitud()) + " km de ti";
        holder.textDistancia.setText(distancia);

        holder.imagePlan.setImageResource(R.drawable.personalogo); // imagen fija por ahora

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, detallesPlan.class);

            intent.putExtra("nombre", plan.getNombre());
            intent.putExtra("categoria", plan.getCategoria());
            intent.putExtra("distancia", distancia);
            intent.putExtra("descripcion", plan.getDescripcion());
            intent.putExtra("direccion", plan.getDireccion());
            intent.putExtra("planId", plan.getId()); // <--- añade esto
            context.startActivity(intent);
        });

    }


    @Override
    public int getItemCount() {
        return listaPlanes.size();
    }

    public class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo, textTipo, textDistancia;
        ImageView imagePlan;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.textTitulo);
            textTipo = itemView.findViewById(R.id.textTipo);
            textDistancia = itemView.findViewById(R.id.textDistancia);
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
