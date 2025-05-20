package com.example.join;

import java.util.Date;

public class PlanItem {
    private String id;
    private String nombre;
    private String categoria;
    private double latitud;
    private double longitud;
    private String descripcion;
    private String direccion;
    private String fotoUrl;
    private Date fechaHora;

    public PlanItem(String nombre, String categoria, double latitud, double longitud) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public PlanItem(String nombre, String categoria, double latitud, double longitud, String descripcion, String direccion) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.latitud = latitud;
        this.longitud = longitud;
        this.descripcion = descripcion;
        this.direccion = direccion;
    }

    public PlanItem(String nombre, String categoria, double latitud, double longitud, String descripcion, String direccion, String fotoUrl, Date fechaHora) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.latitud = latitud;
        this.longitud = longitud;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.fotoUrl = fotoUrl;
        this.fechaHora = fechaHora;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public Date getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Date fechaHora) {
        this.fechaHora = fechaHora;
    }
}
