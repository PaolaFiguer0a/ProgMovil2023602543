<?php
include "conexion.php";

$resultado = $conexion->query("SELECT * FROM JUEGOS");

$juegos = [];

while ($fila = $resultado->fetch_assoc()) {
    $juegos[] = $fila;
}

echo json_encode($juegos);
?>
