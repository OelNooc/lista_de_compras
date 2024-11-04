package com.oelnooc.listadecompras.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.oelnooc.listadecompras.data.dao.ProductoDao
import com.oelnooc.listadecompras.data.model.Producto

@Database(entities = [Producto::class], version = 1)
abstract class ProductoDataBase: RoomDatabase() {
    abstract fun productoDao(): ProductoDao
}