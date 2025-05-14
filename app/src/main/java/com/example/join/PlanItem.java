package com.example.join;

public class PlanItem {
    private String nombre;
    private String categoria;
    private double latitud;
    private double longitud;
    private String descripcion;
    private String direccion;

    public PlanItem(String nombre, String categoria, double latitud, double longitud) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.latitud = latitud;
        this.longitud = longitud;
    }


    public PlanItem() {} // Requerido para Firestore

    public PlanItem(String nombre, String categoria, double latitud, double longitud, String descripcion, String direccion) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.latitud = latitud;
        this.longitud = longitud;
        this.descripcion = descripcion;
        this.direccion = direccion;
    }


    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getDireccion() {
        return direccion;
    }
}

