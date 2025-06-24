<?php
include "conexion.php";

$data = json_decode(file_get_contents("php://input"), true);

if (
    isset($data["id_comentario"]) &&
    isset($data["comentario"]) &&
    isset($data["id_juego"]) &&
    isset($data["id_usuario"])
) {
    $stmt = $conexion->prepare("
        REPLACE INTO COMENTS (id_comentario, comentario, id_juego, id_usuario)
        VALUES (?, ?, ?, ?)
    ");
    $stmt->bind_param(
        "ssss",
        $data["id_comentario"],
        $data["comentario"],
        $data["id_juego"],
        $data["id_usuario"]
    );

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
