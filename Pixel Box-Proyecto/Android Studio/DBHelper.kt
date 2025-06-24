package com.example.proyecto.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.proyecto.Juego
import org.json.JSONObject

class DBHelper(context: Context) : SQLiteOpenHelper(context, "pixelbox.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS JUEGOS (
                id_juego TEXT PRIMARY KEY,
                nombre TEXT,
                plataforma TEXT,
                genero TEXT,
                calificacion TEXT,
                estado TEXT,
                opinion TEXT,
                imagen TEXT,
                eliminado INTEGER DEFAULT 0,
                fecha_mod TEXT
            );
        """
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS USUARIO (
                id_usuario TEXT PRIMARY KEY,
                nom_usuario TEXT,
                correo TEXT,
                contrasena TEXT
            );
        """
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS COMENTS (
                id_comentario TEXT PRIMARY KEY,
                comentario TEXT,
                id_juego TEXT,
                id_usuario TEXT,
                FOREIGN KEY (id_juego) REFERENCES JUEGOS(id_juego),
                FOREIGN KEY (id_usuario) REFERENCES USUARIO(id_usuario)
            );
        """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS JUEGOS")
        db.execSQL("DROP TABLE IF EXISTS USUARIO")
        db.execSQL("DROP TABLE IF EXISTS COMENTS")
        onCreate(db)
    }

    fun obtenerTodosJuegos(): List<JSONObject> {
        val lista = mutableListOf<JSONObject>()
        readableDatabase.rawQuery("SELECT * FROM JUEGOS", null).use { cursor ->
            while (cursor.moveToNext()) {
                val obj = JSONObject().apply {
                    put("id_juego", cursor.getString(0))
                    put("nombre", cursor.getString(1))
                    put("plataforma", cursor.getString(2))
                    put("genero", cursor.getString(3))
                    put("calificacion", cursor.getString(4))
                    put("estado", cursor.getString(5))
                    put("opinion", cursor.getString(6))
                    put("imagen", cursor.getString(7))
                    put("eliminado", cursor.getInt(8))
                    put("fecha_mod", cursor.getString(9))
                }
                lista.add(obj)
            }
        }
        return lista
    }

    fun insertarJuegoSiNoExiste(juego: Juego) {
        val db = writableDatabase
        val cursor =
            db.rawQuery("SELECT id_juego FROM JUEGOS WHERE id_juego = ?", arrayOf(juego.id_juego))
        val existe = cursor.moveToFirst()
        cursor.close()

        if (!existe) {
            val valores = ContentValues().apply {
                put("id_juego", juego.id_juego)
                put("fecha_mod", juego.fecha_mod)
                put("eliminado", if (juego.eliminado) 1 else 0)
                put("nombre", juego.nombre)
                put("plataforma", juego.plataforma)
                put("genero", juego.genero)
                put("calificacion", juego.calificacion)
                put("estado", juego.estado)
                put("opinion", juego.opinion)
                put("imagen", juego.imagen)
            }
            db.insert("JUEGOS", null, valores)
        }
    }

    fun insertarUsuario(u: JSONObject) {
        val valores = ContentValues().apply {
            put("id_usuario", u.getString("id_usuario"))
            put("nom_usuario", u.getString("nom_usuario"))
            put("correo", u.getString("correo"))
            put("contrasena", u.getString("contrasena"))
        }
        writableDatabase.insertWithOnConflict(
            "USUARIO",
            null,
            valores,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun insertarJuego(j: JSONObject) {
        val valores = ContentValues().apply {
            put("id_juego", j.getString("id_juego"))
            put("nombre", j.getString("nombre"))
            put("plataforma", j.getString("plataforma"))
            put("genero", j.getString("genero"))
            put("calificacion", j.getString("calificacion"))
            put("estado", j.getString("estado"))
            put("opinion", j.getString("opinion"))
            put("imagen", j.getString("imagen"))
            put("eliminado", j.getInt("eliminado"))
            put("fecha_mod", j.getString("fecha_mod"))
        }
        writableDatabase.insertWithOnConflict(
            "JUEGOS",
            null,
            valores,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun obtenerTodosUsuarios(): List<JSONObject> {
        val lista = mutableListOf<JSONObject>()
        readableDatabase.rawQuery("SELECT * FROM USUARIO", null).use { cursor ->
            while (cursor.moveToNext()) {
                val obj = JSONObject().apply {
                    put("id_usuario", cursor.getString(0))
                    put("nom_usuario", cursor.getString(1))
                    put("correo", cursor.getString(2))
                    put("contrasena", cursor.getString(3))
                }
                lista.add(obj)
            }
        }
        return lista
    }

    fun obtenerComentariosPorJuego(idJuego: String): List<JSONObject> {
        val lista = mutableListOf<JSONObject>()
        val query = """
            SELECT COMENTS.comentario, USUARIO.nom_usuario
            FROM COMENTS
            LEFT JOIN USUARIO ON COMENTS.id_usuario = USUARIO.id_usuario
            WHERE COMENTS.id_juego = ?
        """
        readableDatabase.rawQuery(query, arrayOf(idJuego)).use { cursor ->
            while (cursor.moveToNext()) {
                val obj = JSONObject().apply {
                    put("comentario", cursor.getString(0))
                    put("usuario", cursor.getString(1) ?: "Anónimo")
                }
                lista.add(obj)
            }
        }
        return lista
    }

    fun asegurarUsuarioExiste(idUsuario: String, nombre: String = "Tú") {
        readableDatabase.rawQuery(
            "SELECT id_usuario FROM USUARIO WHERE id_usuario = ?",
            arrayOf(idUsuario)
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                val valores = ContentValues().apply {
                    put("id_usuario", idUsuario)
                    put("nom_usuario", nombre)
                    put("correo", "$idUsuario@comentario.local")
                    put("contrasena", "1234")
                }
                writableDatabase.insert("USUARIO", null, valores)
            }
        }
    }

    fun insertarComentario(comentario: JSONObject) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id_comentario", comentario.getString("id_comentario"))
            put("comentario", comentario.getString("comentario"))
            put("id_juego", comentario.getString("id_juego"))
            put("id_usuario", comentario.getString("id_usuario"))
        }
        db.insertWithOnConflict("COMENTS", null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }


    fun verificarUsuario(correo: String, contrasena: String): JSONObject? {
        readableDatabase.rawQuery(
            "SELECT * FROM USUARIO WHERE correo = ? AND contrasena = ?",
            arrayOf(correo, contrasena)
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                JSONObject().apply {
                    put("id_usuario", cursor.getString(0))
                    put("nom_usuario", cursor.getString(1))
                    put("correo", cursor.getString(2))
                    put("contrasena", cursor.getString(3))
                }
            } else null
        }
    }

    fun existeCorreo(correo: String): Boolean {
        readableDatabase.rawQuery("SELECT * FROM USUARIO WHERE correo = ?", arrayOf(correo))
            .use { cursor ->
                return cursor.moveToFirst()
            }
    }

    fun obtenerComentariosLocales(): List<JSONObject> {
        val lista = mutableListOf<JSONObject>()
        readableDatabase.rawQuery("SELECT * FROM COMENTS", null).use { cursor ->
            while (cursor.moveToNext()) {
                val obj = JSONObject().apply {
                    put("id_comentario", cursor.getString(0))
                    put("comentario", cursor.getString(1))
                    put("id_juego", cursor.getString(2))
                    put("id_usuario", cursor.getString(3))
                }
                lista.add(obj)
            }
        }
        return lista
    }

    fun insertarJuego(juego: Juego) {
        val valores = ContentValues().apply {
            put("id_juego", juego.id_juego)
            put("nombre", juego.nombre)
            put("plataforma", juego.plataforma)
            put("genero", juego.genero)
            put("calificacion", juego.calificacion.toString())
            put("estado", juego.estado)
            put("opinion", juego.opinion)
            put("imagen", juego.imagen)
            put("eliminado", if (juego.eliminado) 1 else 0)
            put("fecha_mod", juego.fecha_mod)
        }
        writableDatabase.insertWithOnConflict(
            "JUEGOS",
            null,
            valores,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }
}
