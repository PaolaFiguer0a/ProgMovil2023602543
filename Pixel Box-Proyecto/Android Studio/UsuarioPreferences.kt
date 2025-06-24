package com.example.proyecto

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "usuario_prefs")

object UsuarioPreferences {
    private val COLOR_USUARIO = intPreferencesKey("color_usuario")

    suspend fun guardarColorUsuario(context: Context, color: Color) {
        context.dataStore.edit { prefs ->
            prefs[COLOR_USUARIO] = color.toArgb()
        }
    }

    fun obtenerColorUsuario(context: Context): Flow<Color> {
        val colorDefecto = Color(0xFFfdcc28)
        return context.dataStore.data.map { prefs ->
            val colorInt = prefs[COLOR_USUARIO] ?: colorDefecto.toArgb()
            Color(colorInt)
        }
    }
}
