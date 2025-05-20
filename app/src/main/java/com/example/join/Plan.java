package com.example.join;

public class Plan {
    public String nombre;
    public String descripcion;
    public String direccion;
    public String fecha;
    public String hora;
    public String categoria;
    public int participantes;
    public boolean soloAmigos;
    public double latitud;
    public double longitud;

    public Plan() {
        // Constructor vacío necesario para Firebase
    }

    public Plan(String nombre, String descripcion, String direccion, String fecha, String hora,
                String categoria, int participantes, boolean soloAmigos, double latitud, double longitud) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.fecha = fecha;
        this.hora = hora;
        this.categoria = categoria;
        this.participantes = participantes;
        this.soloAmigos = soloAmigos;
        this.latitud = latitud;
        this.longitud = longitud;
    }

}
