package com.oelnooc.listadecompras.ui.viewmodel

import android.content.pm.ActivityInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oelnooc.listadecompras.data.model.Producto
import com.oelnooc.listadecompras.data.repository.ProductoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductoViewModel(private val repository: ProductoRepository) : ViewModel() {

    private val _orientation = MutableLiveData<Int>()
    val orientation: LiveData<Int> get() = _orientation
    private val _productoAEditar = MutableLiveData<Producto?>()
    val productoAEditar: LiveData<Producto?> get() = _productoAEditar

    init {
        _orientation.value = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun mostrarDialogoEditar(producto: Producto) {
        _productoAEditar.value = producto
    }

    fun ocultarDialogoEditar() {
        _productoAEditar.value = null
    }

    fun cambiarEstadoProducto(producto: Producto) {
        viewModelScope.launch {
            val productoActualizado = producto.copy(estadoDeCompra = !producto.estadoDeCompra)
            Log.d("ProductoViewModel", "Cambiando estado a: ${productoActualizado.estadoDeCompra} para: ${productoActualizado.nombre}")
            repository.actualizarProducto(productoActualizado)
        }
    }

    val productos = repository.obtenerProductos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun agregarProducto(nombre: String) {
        viewModelScope.launch {
            repository.agregarProducto(nombre)
        }
    }

    fun actualizarProducto(producto: Producto) {
        viewModelScope.launch {
            repository.actualizarProducto(producto)
        }
    }

    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch {
            repository.eliminarProducto(producto)
        }
    }
}