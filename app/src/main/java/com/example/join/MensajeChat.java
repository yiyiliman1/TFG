package com.example.join;

import com.google.firebase.Timestamp;

public class MensajeChat {
    private String texto;
    private String autorId;
    private String autorNombre;
    private Timestamp timestamp;
    private String tipo;

    public MensajeChat() {

    }

    public MensajeChat(String texto, String autorId, String autorNombre, Timestamp timestamp, String tipo) {
        this.texto = texto;
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.timestamp = timestamp;
        this.tipo = tipo;
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

    public String getTipo() {
        return tipo;
    }
}
