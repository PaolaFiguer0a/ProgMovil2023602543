<?php
include "conexion.php";

$resultado = $conexion->query("SELECT * FROM USUARIO");

$usuarios = [];

while ($fila = $resultado->fetch_assoc()) {
    $usuarios[] = $fila;
}

echo json_encode($usuarios);
?>
