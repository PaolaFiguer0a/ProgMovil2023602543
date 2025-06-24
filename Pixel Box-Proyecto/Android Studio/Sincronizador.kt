package com.example.proyecto.data

import android.content.Context
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.proyecto.Juego

suspend fun sincronizarJuegosDesdeServidor(context: Context, juegos: SnapshotStateList<Juego>) {
    val db = DBHelper(context)

    try {
        val juegosRemotos = RetrofitClient.api.obtenerJuegos()

        juegos.clear()

        for (juego in juegosRemotos) {
            juegos.add(juego)
            db.insertarJuegoSiNoExiste(juego)
        }
    } catch (e: Exception) {
        Log.e("SYNC", "Error al sincronizar: ${e.message}")
        throw e
    }
}
