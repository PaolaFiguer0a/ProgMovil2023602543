<?php
include "conexion.php";

$data = json_decode(file_get_contents("php://input"), true);

if (
    isset($data["id_juego"]) &&
    isset($data["fecha_mod"]) &&
    isset($data["eliminado"]) &&
    isset($data["nombre"]) &&
    isset($data["plataforma"]) &&
    isset($data["genero"]) &&
    isset($data["calificacion"]) &&
    isset($data["estado"])
) {
    $id_juego = $data["id_juego"];
    $fecha_mod = $data["fecha_mod"];
    $eliminado = $data["eliminado"];
    $nombre = $data["nombre"];
    $plataforma = $data["plataforma"];
    $genero = $data["genero"];
    $calificacion = $data["calificacion"];
    $estado = $data["estado"];
    $opinion = $data["opinion"] ?? null;
    $imagen = $data["imagen"] ?? null;

    $stmt = $conexion->prepare("
        REPLACE INTO JUEGOS (id_juego, fecha_mod, eliminado, nombre, plataforma, genero, calificacion, estado, opinion, imagen) 
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ");
    $stmt->bind_param("ssissssdss", $id_juego, $fecha_mod, $eliminado, $nombre, $plataforma, $genero, $calificacion, $estado, $opinion, $imagen);

    if ($stmt->execute()) {
        echo json_encode(["success" => true]);
    } else {
        echo json_encode(["success" => false, "error" => $stmt->error]);
    }

    $stmt->close();
} else {
    echo json_encode(["success" => false, "error" => "Faltan parámetros"]);
}
?>
