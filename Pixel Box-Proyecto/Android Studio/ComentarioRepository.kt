package com.example.proyecto.data

import com.example.proyecto.Comentario
import org.json.JSONObject

object ComentarioRepository {
    suspend fun enviarComentario(comentario: JSONObject) {
        val api = RetrofitClient.api
        val obj = Comentario(
            id_comentario = comentario.getString("id_comentario"),
            comentario = comentario.getString("comentario"),
            id_juego = comentario.getString("id_juego"),
            id_usuario = comentario.getString("id_usuario")
        )
        val response = api.insertarComentario(obj)
        if (!response.isSuccessful) {
            throw Exception("Error: ${response.code()}")
        }
    }
}
