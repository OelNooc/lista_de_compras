package com.oelnooc.listadecompras.ui.view

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.oelnooc.listadecompras.R
import com.oelnooc.listadecompras.data.model.Producto
import com.oelnooc.listadecompras.data.repository.ProductoRepository
import com.oelnooc.listadecompras.data.room.ProductoDataBase
import com.oelnooc.listadecompras.ui.theme.ListaDeComprasTheme
import com.oelnooc.listadecompras.ui.viewmodel.ProductoViewModel
import com.oelnooc.listadecompras.ui.viewmodel.ProductoViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ProductoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            ProductoDataBase::class.java, "productos"
        ).build()

        val productoDao = db.productoDao()
        val repository = ProductoRepository(productoDao)
        val factory = ProductoViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[ProductoViewModel::class.java]

        viewModel.orientation.observe(this, Observer { orientation ->
            requestedOrientation = orientation
        })

        enableEdgeToEdge()
        setContent {
            ListaDeComprasTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: ProductoViewModel = viewModel()) {
    val productos by viewModel.productos.collectAsState()
    Log.d("MainScreen", "Productos: $productos")
    val productoAEditar by viewModel.productoAEditar.observeAsState()
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoAgregar = true }) {
                Text(text = stringResource(id = R.string.add))
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (productos.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .wrapContentSize(Alignment.Center)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(text = stringResource(id = R.string.nothing_to_show))
                        Text(text = stringResource(id = R.string.press_add))
                    }
                }
            } else {
                LazyColumn {
                    items(productos) { producto ->
                        ProductoItem(
                            producto = producto,
                            onEliminar = { viewModel.eliminarProducto(producto) },
                            onToggleEstado = { viewModel.cambiarEstadoProducto(producto) },
                            onEditarNombre = { viewModel.mostrarDialogoEditar(producto) }
                        )
                    }
                }
            }

            if (mostrarDialogoAgregar) {
                AgregarProductoDialog(
                    onGuardar = { nombreNuevo ->
                        viewModel.agregarProducto(nombreNuevo)
                        mostrarDialogoAgregar = false
                    },
                    onCancelar = { mostrarDialogoAgregar = false }
                )
            }

            productoAEditar?.let { producto ->
                EditarProductoDialog(
                    producto = producto,
                    onGuardar = { nombreNuevo ->
                        viewModel.actualizarProducto(producto.copy(nombre = nombreNuevo))
                        viewModel.ocultarDialogoEditar()
                    },
                    onCancelar = { viewModel.ocultarDialogoEditar() }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductoItem(
    producto: Producto,
    onEliminar: () -> Unit,
    onToggleEstado: () -> Unit,
    onEditarNombre: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        val icon = if (producto.estadoDeCompra) R.drawable.check_mark else R.drawable.shopping_cart
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    Log.d("ProductoItem", "onToggleEstado llamado para: ${producto.nombre}")
                    onToggleEstado()
                },
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = producto.nombre,
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = { /* no hace nada */ },
                    onLongClick = onEditarNombre
                )
        )

        Icon(
            painter = painterResource(id = R.drawable.trashcan),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clickable { onEliminar() },
            tint = Color.Unspecified
        )
    }
}

@Composable
fun AgregarProductoDialog(
    onGuardar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var nombreNuevo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onCancelar() },
        title = { Text(text = stringResource(id = R.string.add_product)) },
        text = {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.shopping_cart),
                    contentDescription = "Carrito",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )

                TextField(
                    value = nombreNuevo,
                    onValueChange = { nombreNuevo = it },
                    label = { Text(text = stringResource(id = R.string.product_name)) }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onGuardar(nombreNuevo)
            }) {
                Text(text = stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            Button(onClick = { onCancelar() }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}

@Composable
fun EditarProductoDialog(
    producto: Producto,
    onGuardar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var nombreNuevo by remember { mutableStateOf(producto.nombre) }

    AlertDialog(
        onDismissRequest = { onCancelar() },
        title = { Text(text = stringResource(id = R.string.edit_product)) },
        text = {
            Column {
                TextField(
                    value = nombreNuevo,
                    onValueChange = { nombreNuevo = it },
                    label = { Text(text = stringResource(id = R.string.product_name)) }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onGuardar(nombreNuevo) }) {
                Text(text = stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            Button(onClick = { onCancelar() }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ListaDeComprasTheme {
        MainScreen()
    }
}