<?php
include "conexion.php";

$id_juego = $_GET['id_juego'] ?? null;

if ($id_juego) {
    $stmt = $conexion->prepare("
        SELECT C.comentario, U.nom_usuario 
        FROM COMENTS C
        LEFT JOIN USUARIO U ON C.id_usuario = U.id_usuario
        WHERE C.id_juego = ?
    ");
    $stmt->bind_param("s", $id_juego);
    $stmt->execute();
    $resultado = $stmt->get_result();

    $comentarios = [];

    while ($fila = $resultado->fetch_assoc()) {
        $comentarios[] = $fila;
    }

    echo json_encode($comentarios);
    $stmt->close();
} else {
    echo json_encode(["error" => "Falta el id_juego"]);
}
?>
