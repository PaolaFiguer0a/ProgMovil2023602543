package com.example.proyecto

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.proyecto.ui.theme.ProyectoTheme
import androidx.compose.material.TopAppBar
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.asImageBitmap
import com.example.proyecto.data.ComentarioRepository
import com.example.proyecto.data.DBHelper
import com.example.proyecto.data.RetrofitClient
import com.example.proyecto.data.sincronizarJuegosDesdeServidor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ProyectoTheme {
                var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
                var mostrarDialogo by remember { mutableStateOf(false) }

                val getImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    selectedImageUri = uri
                }
                ControlInicio(
                    onSelectImage = {
                        selectedImageUri = null
                        getImageLauncher.launch("image/*")
                    },
                    selectedImageUri = selectedImageUri,
                    onDismissDialog = { selectedImageUri = null },
                    mostrarDialogo = mostrarDialogo,
                    onMostrarDialogoChange = { mostrarDialogo = it }
                )
            }
        }
    }
}

enum class BottomBarScreen(val icon: ImageVector, val label: String) {
    Inicio(Icons.Default.Home, "Inicio"),
    Buscar(Icons.Default.Search, "Buscar"),
    Personalizar(Icons.Default.Brush, "Personalizar"),
    Perfil(Icons.Default.Person, "Perfil")
}

fun esColorClaro(color: Color): Boolean {
    val luminancia = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    return luminancia > 0.5
}


@Composable
fun ControlInicio(
    onSelectImage: () -> Unit,
    selectedImageUri: Uri?,
    onDismissDialog: () -> Unit,
    mostrarDialogo: Boolean,
    onMostrarDialogoChange: (Boolean) -> Unit,
    usuarioViewModel: UsuarioViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedColor by usuarioViewModel.colorSeleccionado.collectAsState()
    val juegos by usuarioViewModel.listaJuegos.collectAsState()

    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        usuarioViewModel.cargarUsuario(context)
        usuarioViewModel.cargarColor()
        usuarioViewModel.cargarJuegos(context)
        delay(2000)
        loading = false
    }
    if (loading) {
        PantallaCarga(selectedColor)
    } else {
        MainScreen(
            onSelectImage = onSelectImage,
            selectedImageUri = selectedImageUri,
            onDismissDialog = onDismissDialog,
            mostrarDialogo = mostrarDialogo,
            onMostrarDialogoChange = onMostrarDialogoChange,
            juegos = juegos.toMutableStateList()
        )
    }
}


@Composable
fun PantallaCarga(selectedColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(selectedColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Pixel Box",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF091B25)
        )
    }
}


@Composable
fun MainScreen(
    onSelectImage: () -> Unit,
    selectedImageUri: Uri?,
    onDismissDialog: () -> Unit,
    mostrarDialogo: Boolean,
    onMostrarDialogoChange: (Boolean) -> Unit,
    juegos: MutableList<Juego>
) {
    val usuarioViewModel: UsuarioViewModel = viewModel()
    val selectedColor by usuarioViewModel.colorSeleccionado.collectAsState()

    LaunchedEffect(Unit) {
        usuarioViewModel.cargarColor()
    }

    var currentScreen by remember { mutableStateOf(BottomBarScreen.Inicio) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF222222)) {
                BottomBarScreen.values().forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = screen.label,
                                tint = if (currentScreen == screen) selectedColor else selectedColor.copy(alpha = 0.7f)
                            )
                        },
                        label = { Text(screen.label, color = Color.White) },
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                BottomBarScreen.Inicio -> PantallaInicio(
                    juegos = juegos,
                    onSelectImage = onSelectImage,
                    selectedImageUri = selectedImageUri,
                    mostrarDialogo = mostrarDialogo,
                    onMostrarDialogoChange = onMostrarDialogoChange,
                    onDismissDialog = onDismissDialog
                )
                BottomBarScreen.Buscar -> PantallaBuscar(juegos, selectedColor)
                BottomBarScreen.Perfil -> PantallaPerfil(
                    selectedColor = selectedColor,
                    onColorSelected = { usuarioViewModel.actualizarColor(it) }
                )
                BottomBarScreen.Personalizar -> PantallaPersonalizar(usuarioViewModel)
            }
        }
    }
}


@Composable
fun PantallaInicio(
    juegos: MutableList<Juego>,
    onSelectImage: () -> Unit,
    selectedImageUri: Uri?,
    mostrarDialogo: Boolean,
    onMostrarDialogoChange: (Boolean) -> Unit,
    onDismissDialog: () -> Unit,
    usuarioViewModel: UsuarioViewModel = viewModel()
) {
    val context = LocalContext.current
    val idUsuario by usuarioViewModel.idUsuario.collectAsState()
    val selectedColor by usuarioViewModel.colorSeleccionado.collectAsState()
    val textoColor = if (esColorClaro(selectedColor)) Color.Black else Color.White
    var showInfoDialog by remember { mutableStateOf(false) }

    val juegos = remember { mutableStateListOf<Juego>() }
    var mostrarDetalles by remember { mutableStateOf(false) }
    var juegoSeleccionado by remember { mutableStateOf<Juego?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val db = DBHelper(context)
        juegos.clear()
        db.obtenerTodosJuegos().forEach {
            juegos.add(
                Juego(
                    id_juego = it.getString("id_juego"),
                    fecha_mod = it.getString("fecha_mod"),
                    eliminado = it.getInt("eliminado") == 1,
                    nombre = it.getString("nombre"),
                    plataforma = it.getString("plataforma"),
                    imagen = it.getString("imagen"),
                    calificacion = it.getDouble("calificacion"),
                    estado = it.getString("estado"),
                    opinion = it.optString("opinion"),
                    genero = it.getString("genero")
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TopAppBar(
            title = {
                Text("Pixel Box", color = Color.White)
            },
            actions = {
                IconButton(onClick = { showInfoDialog = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Información", tint = Color.White)
                }
            },
            backgroundColor = Color(0xFF222222)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                juegos.forEach { juego ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                juegoSeleccionado = juego
                                mostrarDetalles = true
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Image(
                                painter = base64ToImagePainter(juego.imagen),
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(juego.nombre, fontSize = 18.sp, color = Color.White)
                                Text(juego.plataforma, fontSize = 14.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(5) { i ->
                                        val filled = i < (juego.calificacion / 2).toInt()
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (filled) selectedColor else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format("%.1f", juego.calificacion),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),  // El Row se alinea abajo y ocupa todo el ancho
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón sincronizar
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            try {
                                sincronizarJuegosDesdeServidor(context, juegos)
                                Toast.makeText(context, "Sincronización exitosa", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al sincronizar: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    containerColor = selectedColor
                ) {
                    Icon(Icons.Default.Sync, contentDescription = "Sincronizar", tint = Color.Black)
                }
                //Botón agregar
                FloatingActionButton(
                    onClick = { onMostrarDialogoChange(true) },
                    containerColor = selectedColor
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar Juego",
                        tint = Color.Black
                    )
                }
            }
            if (mostrarDialogo) {
                DialogAgregarJuego(
                    onDismiss = {
                        onMostrarDialogoChange(false)
                        onDismissDialog()
                    },
                    onSelectImage = onSelectImage,
                    selectedImageUri = selectedImageUri
                )
            }
            if (mostrarDetalles && juegoSeleccionado != null) {
                DialogJuegoDetalle(
                    juego = juegoSeleccionado!!,
                    onDismiss = { mostrarDetalles = false },
                    idUsuario = idUsuario
                )
            }
            if (showInfoDialog) {
                AlertDialog(
                    onDismissRequest = { showInfoDialog = false },
                    title = { Text("¿Qué es Pixel Box?", color = Color.White) },
                    text = {
                        Text("¡Bienvenido/a! Aquí te explicamos cómo sacarle el máximo provecho a Pixel Box: \n" +
                                    "Agregar reseñas (+)\n" +
                                    "Con el ícono + puedes agregar reseñas de los juegos que estás jugando o que ya terminaste. Califícalos, comparte tu experiencia y aporta datos útiles para que otros usuarios conozcan tu opinión.\n" +
                                    "Inicio\n" +
                                    "En esta sección encontrarás todos los juegos publicados. Podrás ver sus calificaciones, leer opiniones de otros usuarios y también dejar tus propios comentarios.\n" +
                                    "Buscar\n" +
                                    "¿Buscas algo en específico? Usa esta función para encontrar juegos por nombre. Puedes aplicar filtros desde el ícono en la parte superior para obtener resultados más precisos.\n" +
                                    "Personalizar\n" +
                                    "¡Haz tuyo Pixel Box! Aquí puedes personalizar el aspecto de tu app eligiendo tus colores favoritos, cuando y como tú quieras.\n" +
                                    "Perfil\n" +
                                    "Desde este apartado puedes crear una cuenta nueva o iniciar sesión si ya estás registrado. Accede a tus reseñas, configuraciones y preferencias.\n" +
                                    "¡Disfruta de la experiencia Pixel Box y comparte tu pasión por los videojuegos con la comunidad!",
                            color = Color.White
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { showInfoDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                        ) {
                            Text("Entendido", color = textoColor)
                        }
                    },
                    containerColor = Color(0xFF222222)
                )
            }
        }
    }
}


@Composable
fun base64ToImagePainter(base64Str: String): Painter {
    return try {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        BitmapPainter(bitmap.asImageBitmap())
    } catch (e: Exception) {
        painterResource(id = R.drawable.ic_launcher_background)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAgregarJuego(
    onDismiss: () -> Unit,
    onSelectImage: () -> Unit,
    selectedImageUri: Uri?
) {
    val context = LocalContext.current
    val usuarioViewModel: UsuarioViewModel = viewModel()
    val selectedColor by usuarioViewModel.colorSeleccionado.collectAsState()
    val textoColor = if (esColorClaro(selectedColor)) Color.Black else Color.White

    var nombre by remember { mutableStateOf("") }
    var plataforma by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var calificacion by remember { mutableStateOf(5f) }
    var estado by remember { mutableStateOf("") }
    var opinion by remember { mutableStateOf("") }
    var imagenUri by remember { mutableStateOf<Uri?>(null) }

    val plataformas =
        listOf("Xbox", "PlayStation", "PC", "Nintendo Switch", "Celular", "Multiplataforma")
    val estados = listOf("Jugando", "Pendiente", "Terminado")
    val generos = listOf("Acción", "Aventura", "RPG", "Estrategia", "Shooter", "Deportes", "Indie", "Puzzle", "Simulación", "Construcción", "Carreras", "Otro")

    var expandedPlataforma by remember { mutableStateOf(false) }
    var expandedEstado by remember { mutableStateOf(false) }
    var expandedGenero by remember { mutableStateOf(false) }

    LaunchedEffect(selectedImageUri) {
        if (selectedImageUri != null) {
            imagenUri = selectedImageUri
        }
    }

    fun uriToBase64(uri: Uri, context: Context): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val byteArray = outputStream.toByteArray()

            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF222222)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Agregar Juego",
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre", color = Color.White) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = selectedColor,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = selectedColor,
                        focusedLabelColor = selectedColor,
                        unfocusedLabelColor = Color.Gray,
                        textColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                //Plataforma
                ExposedDropdownMenuBox(
                    expanded = expandedPlataforma,
                    onExpandedChange = { expandedPlataforma = !expandedPlataforma },
                ) {
                    OutlinedTextField(
                        value = plataforma,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Plataforma", color = Color.White) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPlataforma) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = selectedColor,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = selectedColor,
                            focusedLabelColor = selectedColor,
                            unfocusedLabelColor = Color.Gray,
                            textColor = Color.White
                        ),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expandedPlataforma,
                        onDismissRequest = { expandedPlataforma = false },
                        modifier = Modifier.background(selectedColor)
                    ) {
                        plataformas.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion, color = textoColor) },
                                onClick = {
                                    plataforma = opcion
                                    expandedPlataforma = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                //Genero
                ExposedDropdownMenuBox(
                    expanded = expandedGenero,
                    onExpandedChange = { expandedGenero = !expandedGenero },
                ) {
                    OutlinedTextField(
                        value = genero,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Género", color = Color.White) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGenero) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = selectedColor,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = selectedColor,
                            focusedLabelColor = selectedColor,
                            unfocusedLabelColor = Color.Gray,
                            textColor = Color.White
                        ),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expandedGenero,
                        onDismissRequest = { expandedGenero = false },
                        modifier = Modifier.background(selectedColor)
                    ) {
                        generos.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion, color = textoColor) },
                                onClick = {
                                    genero = opcion
                                    expandedGenero = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Calificación: ${calificacion.toInt()}",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start)
                )
                Slider(
                    value = calificacion,
                    onValueChange = { calificacion = it },
                    valueRange = 0f..10f,
                    colors = SliderDefaults.colors(
                        thumbColor = selectedColor,
                        activeTrackColor = selectedColor,
                        inactiveTrackColor = selectedColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                //Estado
                ExposedDropdownMenuBox(
                    expanded = expandedEstado,
                    onExpandedChange = { expandedEstado = !expandedEstado }
                ) {
                    OutlinedTextField(
                        value = estado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado", color = Color.White) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstado) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = selectedColor,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = selectedColor,
                            focusedLabelColor = selectedColor,
                            unfocusedLabelColor = Color.Gray,
                            textColor = Color.White
                        ),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expandedEstado,
                        onDismissRequest = { expandedEstado = false },
                        modifier = Modifier.background(selectedColor)
                    ) {
                        estados.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion, color = textoColor) },
                                onClick = {
                                    estado = opcion
                                    expandedEstado = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = opinion,
                    onValueChange = { opinion = it },
                    label = { Text("Opinión", color = Color.White) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = selectedColor,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = selectedColor,
                        focusedLabelColor = selectedColor,
                        unfocusedLabelColor = Color.Gray,
                        textColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .border(2.dp, selectedColor, RoundedCornerShape(8.dp))
                        .clickable { onSelectImage() },
                    contentAlignment = Alignment.Center
                ) {
                    if (imagenUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imagenUri),
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = "Seleccionar imagen",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                if (imagenUri != null) {
                    TextButton(onClick = { imagenUri = null }) {
                        Text("Eliminar imagen", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                    ) {
                        Text("Cancelar", color = textoColor)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nombre.isBlank() || plataforma.isBlank() || genero.isBlank() ||
                                estado.isBlank() || opinion.isBlank() || imagenUri == null
                            ) {
                                Toast.makeText(
                                    context,
                                    "Por favor, completa todos los campos",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            val base64Imagen = uriToBase64(imagenUri!!, context)
                            if (base64Imagen == null) {
                                Toast.makeText(
                                    context,
                                    "Error al procesar la imagen",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            val nuevoJuego = Juego(
                                id_juego = UUID.randomUUID().toString(),
                                fecha_mod = SimpleDateFormat(
                                    "yyyy-MM-dd'T'HH:mm:ss",
                                    Locale.getDefault()
                                ).format(Date()),
                                eliminado = false,
                                nombre = nombre,
                                plataforma = plataforma,
                                imagen = base64Imagen,
                                calificacion = calificacion.toDouble(),
                                estado = estado,
                                opinion = opinion,
                                genero = genero
                            )
                            usuarioViewModel.agregarJuego(nuevoJuego, context)
                            Toast.makeText(
                                context,
                                "Juego agregado correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                    ) {
                        Text("Agregar", color = textoColor)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    selectedColor: Color
) {
    val textoColor = if (esColorClaro(selectedColor)) Color.Black else Color.White
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = Color.White) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = selectedColor,
                unfocusedBorderColor = Color.Gray,
                cursorColor = selectedColor,
                focusedLabelColor = selectedColor,
                unfocusedLabelColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(selectedColor)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(option, color = textoColor)
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


@Composable
    fun DialogJuegoDetalle(
        juego: Juego,
        onDismiss: () -> Unit,
        idUsuario: String
    ) {
        val context = LocalContext.current
        val dbHelper = remember { DBHelper(context) }
        val usuarioViewModel: UsuarioViewModel = viewModel()
        val selectedColor by usuarioViewModel.colorSeleccionado.collectAsState()
        val textoColor = if (esColorClaro(selectedColor)) Color.Black else Color.White

        val comentarios = remember { mutableStateListOf<JSONObject>() }
        var comentarioTexto by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            comentarios.clear()
            if (juego.id_juego.isNotEmpty()) {
                comentarios.addAll(dbHelper.obtenerComentariosPorJuego(juego.id_juego))
            }
        }

        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF222222),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                Text(
                        text = juego.nombre,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        painter = base64ToImagePainter(juego.imagen),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Plataforma: ${juego.plataforma}", color = Color.Gray)
                    Text("Género: ${juego.genero}", color = Color.Gray)
                    Text("Estado: ${juego.estado}", color = Color.Gray)
                    Text(
                        "Calificación: ${String.format("%.1f", juego.calificacion)}",
                        color = Color.Gray
                    )
                    Text("Opinión: ${juego.opinion}", color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Comentarios", fontSize = 18.sp, color = selectedColor)

                    Spacer(modifier = Modifier.height(8.dp))

                    comentarios.forEach {
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            Text(
                                "${it.optString("usuario", "Anónimo")}:",
                                fontWeight = FontWeight.Bold,
                                color = textoColor
                            )
                            Text(it.optString("comentario", ""), color = Color.White)
                        }
                    }

                    OutlinedTextField(
                        value = comentarioTexto,
                        onValueChange = { comentarioTexto = it },
                        label = { Text("Escribe un comentario", color = Color.White) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = selectedColor,
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = selectedColor,
                            textColor = Color.White,
                            focusedLabelColor = selectedColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val scope = rememberCoroutineScope()

                        Button(
                            onClick = {
                                scope.launch {
                                    if (comentarioTexto.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Escribe un comentario antes de enviar",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@launch
                                    }
                                    if (idUsuario.isEmpty()) {
                                        Toast.makeText(
                                            context,
                                            "Debes iniciar sesión para comentar",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@launch
                                    }

                                    dbHelper.asegurarUsuarioExiste(idUsuario)

                                    val nuevoComentario = JSONObject().apply {
                                        put("id_comentario", UUID.randomUUID().toString())
                                        put("comentario", comentarioTexto)
                                        put("id_juego", juego.id_juego)
                                        put("id_usuario", idUsuario)
                                    }

                                    dbHelper.insertarComentario(nuevoComentario)

                                    try {
                                        withContext(Dispatchers.IO) {
                                            ComentarioRepository.enviarComentario(nuevoComentario)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("SYNC", "Error al subir el comentario: ${e.message}")
                                    }

                                    comentarios.clear()
                                    comentarios.addAll(dbHelper.obtenerComentariosPorJuego(juego.id_juego))

                                    comentarioTexto = ""
                                    Toast.makeText(
                                        context,
                                        "Comentario agregado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                        ) {
                            Text("Comentar", color = textoColor)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                        ) {
                            Text("Cerrar", color = textoColor)
                        }
                    }
                }
            }

        }

    }

@Composable
fun PantallaBuscar(juegos: List<Juego>, selectedColor: Color) {
    var textoBusqueda by remember { mutableStateOf("") }
    var filtroAbierto by remember { mutableStateOf(false) }

    var generoSeleccionado by remember { mutableStateOf("Todas") }
    var plataformaSeleccionada by remember { mutableStateOf("Todas") }
    var calificacionMinima by remember { mutableStateOf(0.0) }
    var filtrosAplicadosTrigger by remember { mutableStateOf(false) }

    val plataformas = listOf(
        "Todas", "Xbox", "PlayStation", "PC", "Nintendo Switch", "Celular", "Multiplataforma")

    val generos = listOf("Todas", "Acción", "Aventura", "RPG", "Estrategia", "Shooter", "Deportes", "Indie", "Puzzle", "Simulación", "Construcción", "Carreras", "Otro")

    val textoColor = if (esColorClaro(selectedColor)) Color.Black else Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF222222), shape = RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                placeholder = { Text("Buscar juego...", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = selectedColor,
                    unfocusedBorderColor = Color.Gray,
                    backgroundColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(color = Color.White)
            )

            IconButton(onClick = { filtroAbierto = !filtroAbierto }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtrar",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filtros
        if (filtroAbierto) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF222222), shape = RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text("Filtrar por:", color = Color.White)

                Spacer(modifier = Modifier.height(8.dp))

                DropdownSelector(
                    label = "Género",
                    options = generos,
                    selectedOption = generoSeleccionado,
                    onOptionSelected = { generoSeleccionado = it },
                    selectedColor = selectedColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                DropdownSelector(
                    label = "Plataforma",
                    options = plataformas,
                    selectedOption = plataformaSeleccionada,
                    onOptionSelected = { plataformaSeleccionada = it },
                    selectedColor = selectedColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Calificación mínima:", color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = calificacionMinima.toFloat(),
                        onValueChange = { calificacionMinima = it.toDouble() },
                        valueRange = 0f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = selectedColor,
                            activeTrackColor = selectedColor,
                            inactiveTrackColor = selectedColor.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(String.format("%.1f", calificacionMinima), color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            textoBusqueda = ""
                            generoSeleccionado = "Todas"
                            plataformaSeleccionada = "Todas"
                            calificacionMinima = 0.0
                            filtrosAplicadosTrigger = !filtrosAplicadosTrigger
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                    ) {
                        Text("Limpiar filtros", color = textoColor)
                    }

                    Button(
                        onClick = {
                            filtroAbierto = false
                            filtrosAplicadosTrigger = !filtrosAplicadosTrigger
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                    ) {
                        Text("Aplicar filtros", color = textoColor)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Filtrado
        val juegosFiltrados = remember(
            textoBusqueda,
            generoSeleccionado,
            plataformaSeleccionada,
            calificacionMinima,
            filtrosAplicadosTrigger
        ) {
            if (
                textoBusqueda.isBlank() &&
                generoSeleccionado == "Todas" &&
                plataformaSeleccionada == "Todas" &&
                calificacionMinima == 0.0
            ) {
                juegos
            } else {
                juegos.filter { juego ->
                    juego.nombre.contains(textoBusqueda, ignoreCase = true) &&
                            (generoSeleccionado == "Todas" || juego.genero.trim()
                                .equals(generoSeleccionado.trim(), ignoreCase = true)) &&
                            (plataformaSeleccionada == "Todas" || juego.plataforma.trim()
                                .equals(plataformaSeleccionada.trim(), ignoreCase = true)) &&
                            juego.calificacion >= calificacionMinima
                }
            }
        }

        //Resultados
        if (juegosFiltrados.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron resultados",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(juegosFiltrados) { juego ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            val painter = if (!juego.imagen.isNullOrBlank()) {
                                base64ToImagePainter(juego.imagen)
                            } else {
                                painterResource(id = android.R.drawable.ic_menu_gallery)
                            }

                            Image(
                                painter = painter,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(juego.nombre, fontSize = 18.sp, color = Color.White)
                                Text(juego.plataforma, fontSize = 14.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(5) { i ->
                                        val filled = i < (juego.calificacion / 2).toInt()
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (filled) selectedColor else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format("%.1f", juego.calificacion),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
    fun PantallaPerfil(
        selectedColor: Color,
        onColorSelected: (Color) -> Unit,
        usuarioViewModel: UsuarioViewModel = viewModel()
    ) {
        val context = LocalContext.current
        var mostrandoLogin by remember { mutableStateOf(false) }
        var mostrandoRegistro by remember { mutableStateOf(false) }

        var correoLogin by remember { mutableStateOf("") }
        var contrasenaLogin by remember { mutableStateOf("") }

        var nuevoUsuario by remember { mutableStateOf("") }
        var nuevoCorreo by remember { mutableStateOf("") }
        var nuevaContrasena by remember { mutableStateOf("") }

        val nombreUsuario = usuarioViewModel.nombreUsuario.collectAsState().value
        val estaAutenticado = nombreUsuario.isNotEmpty()
        val textoColor = if (esColorClaro(selectedColor)) Color.Black else Color.White

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.LightGray, shape = CircleShape)
                        .padding(16.dp),
                    tint = selectedColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (!estaAutenticado) "Bienvenido/a a Pixel Box" else "Bienvenido/a, $nombreUsuario",
                    fontSize = 24.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (!estaAutenticado) {
                    if (!mostrandoLogin && !mostrandoRegistro) {
                        Button(
                            onClick = { mostrandoLogin = true },
                            colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                        ) {
                            Text("Iniciar Sesión", color = textoColor)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { mostrandoRegistro = true },
                            colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                        ) {
                            Text("Crear Cuenta", color = textoColor)
                        }
                    }

                    if (mostrandoLogin) {
                        OutlinedTextField(
                            value = correoLogin,
                            onValueChange = { correoLogin = it },
                            label = { Text("Correo electrónico", color = Color.White) },
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = selectedColor,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = selectedColor,
                                focusedLabelColor = selectedColor,
                                unfocusedLabelColor = Color.Gray,
                                textColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = contrasenaLogin,
                            onValueChange = { contrasenaLogin = it },
                            label = { Text("Contraseña", color = Color.White) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = selectedColor,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = selectedColor,
                                focusedLabelColor = selectedColor,
                                unfocusedLabelColor = Color.Gray,
                                textColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    if (correoLogin.isBlank() || contrasenaLogin.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Por favor, completa todos los campos",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }
                                    val exito = usuarioViewModel.iniciarSesion(
                                        context,
                                        correoLogin,
                                        contrasenaLogin
                                    )
                                    if (!exito) {
                                        Toast.makeText(
                                            context,
                                            "Credenciales incorrectas",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                            ) {
                                Text("Entrar", color = textoColor)
                            }

                            Button(
                                onClick = {
                                    mostrandoLogin = false
                                    correoLogin = ""
                                    contrasenaLogin = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                            ) {
                                Text("Cancelar", color = textoColor)
                            }
                        }
                    }

                    if (mostrandoRegistro) {
                        OutlinedTextField(
                            value = nuevoUsuario,
                            onValueChange = { nuevoUsuario = it },
                            label = { Text("Nombre de usuario", color = Color.White) },
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = selectedColor,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = selectedColor,
                                focusedLabelColor = selectedColor,
                                unfocusedLabelColor = Color.Gray,
                                textColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = nuevoCorreo,
                            onValueChange = { nuevoCorreo = it },
                            label = { Text("Correo electrónico", color = Color.White) },
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = selectedColor,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = selectedColor,
                                focusedLabelColor = selectedColor,
                                unfocusedLabelColor = Color.Gray,
                                textColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = nuevaContrasena,
                            onValueChange = { nuevaContrasena = it },
                            label = { Text("Contraseña", color = Color.White) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = selectedColor,
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = selectedColor,
                                focusedLabelColor = selectedColor,
                                unfocusedLabelColor = Color.Gray,
                                textColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    if (nuevoUsuario.isBlank() || nuevoCorreo.isBlank() || nuevaContrasena.isBlank()) {
                                        Toast.makeText(
                                            context,
                                            "Por favor, completa todos los campos",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@Button
                                    }
                                    val exito = usuarioViewModel.registrarUsuario(
                                        context,
                                        nuevoUsuario,
                                        nuevoCorreo,
                                        nuevaContrasena
                                    )
                                    if (exito) {
                                        Toast.makeText(
                                            context,
                                            "Usuario registrado correctamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        mostrandoRegistro = false
                                        nuevoUsuario = ""
                                        nuevoCorreo = ""
                                        nuevaContrasena = ""
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Ese correo ya está registrado",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                            ) {
                                Text("Registrarse", color = textoColor)
                            }

                            Button(
                                onClick = {
                                    mostrandoRegistro = false
                                    nuevoUsuario = ""
                                    nuevoCorreo = ""
                                    nuevaContrasena = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                            ) {
                                Text("Cancelar", color = textoColor)
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            usuarioViewModel.cerrarSesion(context)
                            mostrandoLogin = false
                            mostrandoRegistro = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                    ) {
                        Text("Cerrar sesión", color = textoColor)
                    }
                }
            }
        }
    }


@Composable
    fun PantallaPersonalizar(
        usuarioViewModel: UsuarioViewModel
    ) {
        val selectedColor by usuarioViewModel.colorSeleccionado.collectAsState()

        LaunchedEffect(Unit) {
            usuarioViewModel.cargarColor()
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Personalizar App",
                    color = Color.White,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Surface(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF222222)
                ) {
                    ColorPickerContent(
                        initialColor = selectedColor,
                        onColorSelected = {
                            usuarioViewModel.actualizarColor(it)
                        }
                    )
                }
            }
        }
    }


    @Composable
    fun ColorPickerContent(
        initialColor: Color,
        onColorSelected: (Color) -> Unit
    ) {
        var red by remember { mutableStateOf((initialColor.red * 255).toInt()) }
        var green by remember { mutableStateOf((initialColor.green * 255).toInt()) }
        var blue by remember { mutableStateOf((initialColor.blue * 255).toInt()) }

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Selecciona el color", color = Color.White, fontSize = 18.sp)

            Spacer(modifier = Modifier.height(16.dp))

            ColorSlider("Rojo", red, onValueChange = {
                red = it
            })

            ColorSlider("Verde", green, onValueChange = {
                green = it
            })

            ColorSlider("Azul", blue, onValueChange = {
                blue = it
            })

            Spacer(modifier = Modifier.height(16.dp))

            val color = Color(red, green, blue)

            Text("Vista previa del color:  ", color = Color.White, fontSize = 18.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(color, RoundedCornerShape(8.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onColorSelected(color) },
                colors = ButtonDefaults.buttonColors(containerColor = color)
            ) {
                Text("Aplicar", color = Color.Black)
            }
        }
    }


    @Composable
    fun ColorSlider(
        label: String,
        value: Int,
        onValueChange: (Int) -> Unit
    ) {
        Column {
            Text(label, color = Color.White)
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..255f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White
                )
            )
        }
    }
