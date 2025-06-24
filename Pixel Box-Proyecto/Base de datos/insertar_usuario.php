<?php
include "conexion.php";

$data = json_decode(file_get_contents("php://input"), true);

if (
    isset($data["id_usuario"]) &&
    isset($data["nom_usuario"]) &&
    isset($data["correo"]) &&
    isset($data["contrasena"])
) {
    $stmt = $conexion->prepare("
        REPLACE INTO USUARIO (id_usuario, nom_usuario, correo, contrasena)
        VALUES (?, ?, ?, ?)
    ");
    $stmt->bind_param(
        "ssss",
        $data["id_usuario"],
        $data["nom_usuario"],
        $data["correo"],
        $data["contrasena"]
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
