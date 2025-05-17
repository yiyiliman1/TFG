package com.example.join;

import com.google.firebase.Timestamp;

public class MensajeChat {
    private String texto;
    private String autorId;
    private String autorNombre;
    private Timestamp timestamp;

    public MensajeChat() {
        // Constructor vacío requerido por Firestore
    }

    public MensajeChat(String texto, String autorId, String autorNombre, Timestamp timestamp) {
        this.texto = texto;
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.timestamp = timestamp;
    }

    public String getTexto() {
        return texto;
    }

    public String getAutorId() {
        return autorId;
    }

    public String getAutorNombre() {
        return autorNombre;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
