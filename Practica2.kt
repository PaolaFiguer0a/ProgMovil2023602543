package com.example.practica2

import androidx.compose.ui.unit.dp

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
import com.example.practica2.ui.theme.Practica2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Practica2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UIPrincipal(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun UIPrincipal(modifier: Modifier = Modifier) {
    var nombre by remember { mutableStateOf("") }
    val contexto = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Nombre:")
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Introduce tu nombre") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            Toast.makeText(contexto, "Hola, $nombre!", Toast.LENGTH_SHORT).show()
        }) {
            Text("Saludar!")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Previsualizacion() {
    Practica2Theme {
        UIPrincipal()
    }
}

