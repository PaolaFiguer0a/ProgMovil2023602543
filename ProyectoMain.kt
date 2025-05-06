package com.example.pruebas

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.pruebas.ui.theme.PruebasTheme
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream

class MainActivity : ComponentActivity() {

    private var selectedImageBitmap by mutableStateOf<Bitmap?>(null)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dbHelper = DBHelper(this)

        // Solicitar permisos de cámara al inicio
        requestCameraPermission()

        setContent {
            PruebasTheme {
                val context = LocalContext.current
                var productos by remember { mutableStateOf(dbHelper.getAllProductos()) }
                val imagenBitmap = remember { mutableStateOf<Bitmap?>(null) }
                var showAddDialog by remember { mutableStateOf(false) }
                var showEditDialog by remember { mutableStateOf(false) }
                var showDeleteDialog by remember { mutableStateOf(false) } // Variable para controlar el diálogo de confirmación
                var selectedProduct by remember { mutableStateOf<Producto?>(null) }

                // Image picker
                val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                        selectedImageBitmap = imageBitmap
                    }
                }
                val pickImageLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        val inputStream = context.contentResolver.openInputStream(it)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        selectedImageBitmap = bitmap // Asegúrate de actualizar selectedImageBitmap
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Productos") },
                            actions = {
                                IconButton(onClick = {
                                    selectedImageBitmap = null
                                    showAddDialog = true
                                }) {
                                    Text("➕")
                                }
                            }
                        )
                    }
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(it)
                            .fillMaxSize()
                    ) {
                        items(productos) { producto ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    producto.imagen?.let { base64 ->
                                        dbHelper.base64ToBitmap(base64)?.let { bitmap ->
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Imagen",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(150.dp)
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("${producto.nombre}")
                                        Text("${producto.precio}")
                                        Text("${producto.descripcion ?: "Sin descripción"}")
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Button(onClick = {
                                            selectedProduct = producto
                                            showEditDialog = true
                                        }) {
                                            Text("Modificar✏️")
                                        }
                                        Button(onClick = {
                                            selectedProduct = producto
                                            showDeleteDialog = true // Mostrar el diálogo de eliminación
                                        }) {
                                            Text("Eliminar🗑️")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Diálogo para AGREGAR
                    if (showAddDialog) {
                        var nombre by remember { mutableStateOf(TextFieldValue("")) }
                        var precio by remember { mutableStateOf(TextFieldValue("")) }
                        var descripcion by remember { mutableStateOf(TextFieldValue("")) }

                        AlertDialog(
                            onDismissRequest = { showAddDialog = false },
                            confirmButton = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    // Botón de Cancelar (ahora primero)
                                    Button(
                                        onClick = { showAddDialog = false },
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .widthIn(min = 100.dp)
                                    ) {
                                        Text("Cancelar")
                                    }

                                    // Botón de Agregar (ahora después)
                                    Button(
                                        onClick = {
                                            val nombreVal = nombre.text.trim()
                                            val precioVal = precio.text.trim().toDoubleOrNull()

                                            if (nombreVal.isNotEmpty() && precioVal != null) {
                                                // Usamos selectedImageBitmap para ambas imágenes (tomadas y seleccionadas)
                                                val imageBase64 = selectedImageBitmap?.let { bitmapToBase64(it) }

                                                dbHelper.insertProductoDesdeUI(
                                                    nombreVal,
                                                    precioVal,
                                                    descripcion.text.trim(),
                                                    imageBase64
                                                )

                                                productos = dbHelper.getAllProductos()
                                                showAddDialog = false
                                                Toast.makeText(context, "Producto agregado", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Completa los campos", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .widthIn(min = 100.dp)
                                    ) {
                                        Text("Agregar")
                                    }

                                }
                            },
                            title = { Text("Agregar producto") },
                            text = {
                                Column {
                                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                                    OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") })
                                    OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Button(onClick = {
                                            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                            takePictureLauncher.launch(cameraIntent)
                                        }) {
                                            Text("Tomar foto")
                                        }
                                    }

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Button(onClick = {
                                            pickImageLauncher.launch("image/*")
                                        }) {
                                            Text("Seleccionar desde galería")
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        imagenBitmap.value?.let { bitmap ->
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Imagen seleccionada",
                                                modifier = Modifier
                                                    .size(200.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .border(2.dp, Color.Gray)
                                            )
                                        }
                                    }

                                    selectedImageBitmap?.let {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                        )
                                    }
                                }
                            }
                        )
                    }

                    // Diálogo de eliminación con confirmación
                    if (showDeleteDialog && selectedProduct != null) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            confirmButton = {
                                Button(onClick = {
                                    dbHelper.deleteProducto(selectedProduct!!.id_producto)
                                    productos = dbHelper.getAllProductos()
                                    showDeleteDialog = false
                                    Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("Sí")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showDeleteDialog = false }) {
                                    Text("No")
                                }
                            },
                            title = { Text("Eliminar producto") },
                            text = { Text("¿Estás seguro de que deseas eliminar este producto?") }
                        )
                    }

                    // Diálogo para MODIFICAR
                    if (showEditDialog && selectedProduct != null) {
                        var nombre by remember { mutableStateOf(TextFieldValue(selectedProduct!!.nombre)) }
                        var precio by remember { mutableStateOf(TextFieldValue(selectedProduct!!.precio.toString())) }
                        var descripcion by remember { mutableStateOf(TextFieldValue(selectedProduct!!.descripcion ?: "")) }

                        AlertDialog(
                            onDismissRequest = { showEditDialog = false },
                            confirmButton = {
                                Button(onClick = {
                                    val nombreVal = nombre.text.trim()
                                    val precioVal = precio.text.trim().toDoubleOrNull()

                                    if (nombreVal.isNotEmpty() && precioVal != null) {
                                        dbHelper.actualizarProducto(selectedProduct!!.id_producto.toInt(), nombreVal, precioVal, descripcion.text.trim())
                                        productos = dbHelper.getAllProductos()
                                        showEditDialog = false
                                        Toast.makeText(context, "Producto modificado", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Completa los campos", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Text("Guardar")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showEditDialog = false }) {
                                    Text("Cancelar")
                                }
                            },
                            title = { Text("Modificar producto") },
                            text = {
                                Column {
                                    OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                                    OutlinedTextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") })
                                    OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") })
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }
    }


}
