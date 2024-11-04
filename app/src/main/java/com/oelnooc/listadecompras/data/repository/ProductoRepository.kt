package com.oelnooc.listadecompras.data.repository

import com.oelnooc.listadecompras.data.dao.ProductoDao
import com.oelnooc.listadecompras.data.model.Producto
import kotlinx.coroutines.flow.Flow

class ProductoRepository (private val productoDao: ProductoDao) {

    fun obtenerProductos(): Flow<List<Producto>> = productoDao.obtenerProductos()

    suspend fun agregarProducto(nombre: String) {
        val producto = Producto(nombre = nombre)
        productoDao.insertarProducto(producto)
    }

    suspend fun actualizarProducto(producto: Producto) {
        productoDao.actualizarProducto(producto)
    }

    suspend fun eliminarProducto(producto: Producto) {
        productoDao.eliminarProducto(producto)
    }
}