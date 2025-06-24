package com.example.proyecto

data class Juego(
    val id_juego: String,
    val fecha_mod: String,
    val eliminado: Boolean,
    val nombre: String,
    val plataforma: String,
    val genero: String,
    val calificacion: Double,
    val estado: String,
    val opinion: String,
    val imagen: String
)
