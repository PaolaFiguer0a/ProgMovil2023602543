package com.example.proyecto

import retrofit2.http.*

interface ApiService {

    //JUEGOS
    @GET("get_juegos.php")
    suspend fun obtenerJuegos(): List<Juego>

    @POST("insertar_juego.php")
    suspend fun insertarJuego(@Body juego: Juego): ApiResponse

    //USUARIOS
    @GET("get_usuarios.php")
    suspend fun obtenerUsuarios(): List<Usuario>

    @POST("insertar_usuario.php")
    suspend fun insertarUsuario(@Body usuario: Usuario): ApiResponse

    //COMENTARIOS
    @GET("get_comentarios.php")
    suspend fun obtenerComentarios(@Query("id_juego") idJuego: String): List<Comentario>

    @POST("insertar_comentario.php")
    suspend fun insertarComentario(@Body comentario: Comentario): retrofit2.Response<Unit>
}
