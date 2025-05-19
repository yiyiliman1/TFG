package com.example.join;

public class ChatPrivadoModelo {
    private String chatId;
    private String usuarioId;
    private String nombre;
    private String fotoUrl;

    public ChatPrivadoModelo(String chatId, String usuarioId, String nombre, String fotoUrl) {
        this.chatId = chatId;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.fotoUrl = fotoUrl;
    }

    public String getChatId() {
        return chatId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }
}
