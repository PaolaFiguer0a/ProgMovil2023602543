<?php
$host = "sql102.infinityfree.com"; // 
$usuario = "if0 39299849";         // Tu usuario MySQL
$contrasena = "gXzYfIoSSuC";     // Tu contraseña
$base_datos = "if0_ 39299849_pixelbox"; // Tu base de datos

$conexion = new mysqli($host, $usuario, $contrasena, $base_datos);

if ($conexion->connect_error) {
    die("Conexión fallida: " . $conexion->connect_error);
}
?>
