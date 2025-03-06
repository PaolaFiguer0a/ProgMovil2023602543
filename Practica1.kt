package com.example.practica1

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.practica2.ui.theme.Practica2Theme
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Practica1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UIPrincipal(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun UIPrincipal(modifier: Modifier = Modifier) {
    var opcion by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf("") }
    var input1 by remember { mutableStateOf("") }
    var input2 by remember { mutableStateOf("") }
    var input3 by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    val contexto = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "MENÚ")
        Text(text = "1. Sumar tres números")
        Text(text = "2. Nombre completo")
        Text(text = "3. Calculo de edad")
        Text(text = "4. Salir")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = opcion,
            onValueChange = { opcion = it },
            label = { Text("Ingresa una opción") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        when (opcion) {
            "1" -> {
                OutlinedTextField(value = input1, onValueChange = { input1 = it }, label = { Text("Número 1") })
                OutlinedTextField(value = input2, onValueChange = { input2 = it }, label = { Text("Número 2") })
                OutlinedTextField(value = input3, onValueChange = { input3 = it }, label = { Text("Número 3") })
            }
            "2" -> {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre completo") })
            }
            "3" -> {
                OutlinedTextField(value = fechaNacimiento, onValueChange = { fechaNacimiento = it }, label = { Text("Fecha de nacimiento (YYYY-MM-DD)") })
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            resultado = when (opcion) {
                "1" -> sumarTresNumeros(input1, input2, input3)
                "2" -> ingresarNombre(nombre)
                "3" -> calcularTiempoVida(fechaNacimiento)
                "4" -> "Programa finalizado."
                else -> "Opción no válida"
            }
        }) {
            Text("Ejecutar")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = resultado)
    }
}

fun sumarTresNumeros(num1: String, num2: String, num3: String): String {
    return try {
        val suma = num1.toInt() + num2.toInt() + num3.toInt()
        "La suma es: $suma"
    } catch (e: NumberFormatException) {
        "Ingrese números válidos."
    }
}

fun ingresarNombre(nombre: String): String {
    return "Hola, $nombre"
}

fun calcularTiempoVida(fechaNacimiento: String): String {
    return try {
        val fecha = LocalDate.parse(fechaNacimiento)
        val fechaActual = LocalDate.now()
        val edad = Period.between(fecha, fechaActual)
        val diasVividos = ChronoUnit.DAYS.between(fecha, fechaActual)
        "Edad: ${edad.years} años, ${edad.months} meses, ${edad.days} días. Total días vividos: $diasVividos"
    } catch (e: Exception) {
        "Formato de fecha inválido. Use YYYY-MM-DD."
    }
}

@Preview(showBackground = true)
@Composable
fun Previsualizacion() {
    Practica1Theme {
        UIPrincipal()
    }
}
