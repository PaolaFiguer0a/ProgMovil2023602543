package com.example.proyecto.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object SyncManager {

    private const val BASE_URL = "https://pixelbox.infinityfreeapp.com/"

    suspend fun sincronizar(context: Context) {
        withContext(Dispatchers.IO) {
            val db = DBHelper(context)

            try {
                val response = httpGet("get_juegos.php")
                val juegosRemotos = JSONArray(response)
                for (i in 0 until juegosRemotos.length()) {
                    val juegoRemoto = juegosRemotos.getJSONObject(i)
                    db.insertarJuego(juegoRemoto)
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Error al obtener o insertar juegos: ${e.message}")
            }

            try {
                val usuariosResp = httpGet("get_usuarios.php")
                val usuariosRemotos = JSONArray(usuariosResp)
                for (i in 0 until usuariosRemotos.length()) {
                    val usuario = usuariosRemotos.getJSONObject(i)
                    db.insertarUsuario(usuario)
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Error al obtener o insertar usuarios: ${e.message}")
            }

            try {
                val comentsResp = httpGet("get_comentarios.php?id_juego=1")
                val comentsRemotos = JSONArray(comentsResp)
                for (i in 0 until comentsRemotos.length()) {
                    val com = comentsRemotos.getJSONObject(i)
                    db.insertarComentario(com)
                }
            } catch (e: Exception) {
                Log.e("SYNC", "Error al obtener o insertar comentarios: ${e.message}")
            }
        }
    }

    private fun httpGet(endpoint: String): String {
        val url = URL(BASE_URL + endpoint)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.doInput = true

        return try {
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}

suspend fun enviarComentario(nuevoComentario: JSONObject) = withContext(Dispatchers.IO) {
    val url = URL("https://pixelbox.infinityfreeapp.com/insertar_comentario.php")
    (url.openConnection() as HttpURLConnection).run {
        requestMethod = "POST"
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
        outputStream.use {
            it.write(nuevoComentario.toString().toByteArray(Charsets.UTF_8))
        }
        inputStream.bufferedReader().use { it.readText() }
        disconnect()
    }
}

suspend fun enviarAWeb(endpoint: String, json: JSONObject) = withContext(Dispatchers.IO) {
    val url = URL("https://pixelbox.infinityfreeapp.com/$endpoint")
    (url.openConnection() as HttpURLConnection).run {
        requestMethod = "POST"
        doOutput = true
        setRequestProperty("Content-Type", "application/json")
        outputStream.use {
            it.write(json.toString().toByteArray(Charsets.UTF_8))
        }
        inputStream.bufferedReader().use { it.readText() }
        disconnect()
    }
}

suspend fun subirCambiosLocales(context: Context) = withContext(Dispatchers.IO) {
    val db = DBHelper(context)

    // Subir usuarios
    db.obtenerTodosUsuarios().forEach {
        try {
            enviarAWeb("insertar_usuario.php", it)
        } catch (e: Exception) {
            Log.e("SYNC", "Error al subir usuario: ${e.message}")
        }
    }

    // Subir juegos
    db.obtenerTodosJuegos().forEach {
        try {
            enviarAWeb("insertar_juego.php", it)
        } catch (e: Exception) {
            Log.e("SYNC", "Error al subir juego: ${e.message}")
        }
    }

    // Subir comentarios
    db.obtenerComentariosLocales().forEach {
        try {
            enviarAWeb("insertar_comentario.php", it)
        } catch (e: Exception) {
            Log.e("SYNC", "Error al subir comentario: ${e.message}")
        }
    }
}
