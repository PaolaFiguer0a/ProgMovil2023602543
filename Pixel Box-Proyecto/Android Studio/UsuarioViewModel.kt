package com.example.proyecto

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.DBHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

open class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // Usuario
    private val _nombreUsuario = MutableStateFlow("")
    val nombreUsuario: StateFlow<String> get() = _nombreUsuario

    private val _idUsuario = MutableStateFlow("")
    val idUsuario: StateFlow<String> get() = _idUsuario

    // Color seleccionado
    private val _colorSeleccionado = MutableStateFlow(Color.Yellow)
    val colorSeleccionado: StateFlow<Color> get() = _colorSeleccionado

    //Lista de juegos
    private val _listaJuegos = MutableStateFlow<List<Juego>>(emptyList())
    val listaJuegos: StateFlow<List<Juego>> = _listaJuegos

    fun cargarColor() {
        viewModelScope.launch {
            UsuarioPreferences.obtenerColorUsuario(context).collect { color ->
                _colorSeleccionado.value = color
            }
        }
    }

    fun actualizarColor(color: Color) {
        viewModelScope.launch {
            UsuarioPreferences.guardarColorUsuario(context, color)
            _colorSeleccionado.value = color
        }
    }

    fun registrarUsuario(context: Context, nombre: String, correo: String, contrasena: String): Boolean {
        val db = DBHelper(context)
        if (db.existeCorreo(correo)) return false

        val nuevoId = UUID.randomUUID().toString()

        val usuario = JSONObject().apply {
            put("id_usuario", nuevoId)
            put("nom_usuario", nombre)
            put("correo", correo)
            put("contrasena", contrasena)
        }

        db.insertarUsuario(usuario)
        _nombreUsuario.value = nombre
        _idUsuario.value = nuevoId
        guardarSesion(context, nombre, nuevoId)
        return true
    }

    fun iniciarSesion(context: Context, correo: String, contrasena: String): Boolean {
        val db = DBHelper(context)
        val usuario = db.verificarUsuario(correo, contrasena)
        return if (usuario != null) {
            val id = usuario.getString("id_usuario")
            val nombre = usuario.getString("nom_usuario")
            _nombreUsuario.value = nombre
            _idUsuario.value = id
            guardarSesion(context, nombre, id)
            true
        } else {
            false
        }
    }

    fun cerrarSesion(context: Context) {
        _nombreUsuario.value = ""
        _idUsuario.value = ""
        val prefs = context.getSharedPreferences("PixelBoxPrefs", Context.MODE_PRIVATE)
        prefs.edit().remove("usuario").remove("id_usuario").apply()
    }

    fun cargarUsuario(context: Context) {
        val prefs = context.getSharedPreferences("PixelBoxPrefs", Context.MODE_PRIVATE)
        val nombre = prefs.getString("usuario", "") ?: ""
        val id = prefs.getString("id_usuario", "") ?: ""
        _nombreUsuario.value = nombre
        _idUsuario.value = id
    }

    private fun guardarSesion(context: Context, nombre: String, idUsuario: String) {
        val prefs = context.getSharedPreferences("PixelBoxPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("usuario", nombre).putString("id_usuario", idUsuario).apply()
    }

    //Cargar juegos desde BD local
    fun cargarJuegos(context: Context) {
        viewModelScope.launch {
            val dbHelper = DBHelper(context)
            val juegosJson = dbHelper.obtenerTodosJuegos()
            val juegos = juegosJson.map { json ->
                Juego(
                    id_juego = json.getString("id_juego"),
                    fecha_mod = json.getString("fecha_mod"),
                    eliminado = json.getInt("eliminado") == 1,
                    nombre = json.getString("nombre"),
                    plataforma = json.getString("plataforma"),
                    genero = json.getString("genero"),
                    calificacion = json.getString("calificacion").toDoubleOrNull() ?: 0.0,
                    estado = json.getString("estado"),
                    opinion = json.optString("opinion"),
                    imagen = json.optString("imagen")
                )
            }
            _listaJuegos.value = juegos
        }
    }

    fun agregarJuego(juego: Juego, context: Context) {
        viewModelScope.launch {
            val dbHelper = DBHelper(context)
            dbHelper.insertarJuego(juego) // Usamos el método que acepta un Juego directamente
            _listaJuegos.value = _listaJuegos.value + juego
        }
    }
}
