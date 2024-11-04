package com.oelnooc.listadecompras.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var nombre: String,
    var estadoDeCompra: Boolean = false
)
