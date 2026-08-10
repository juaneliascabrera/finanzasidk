package com.finanzas.persistence

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "cuentas")
data class CuentaEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val moneda: String,
    val tipo: String,
    val saldoInicial: String
)

@Entity(tableName = "categorias")
data class CategoriaEntity(
    @PrimaryKey val id: String,
    val nombre: String
)

@Entity(
    tableName = "presupuestos",
    indices = [Index(value = ["categoriaId"])]
)
data class PresupuestoEntity(
    @PrimaryKey val id: String,
    val categoriaId: String,
    val mes: String,
    val limite: String,
    val moneda: String
)

@Entity(
    tableName = "operaciones",
    indices = [
        Index(value = ["ordenRegistro"]),
        Index(value = ["cuentaId", "id"])
    ]
)
data class OperacionEntity(
    @PrimaryKey(autoGenerate = true) val ordenRegistro: Long = 0,
    val id: String,
    val tipo: String,
    val fecha: String,
    val cuentaId: String?,
    val cuentaOrigenId: String?,
    val cuentaDestinoId: String?,
    val monto: String?,
    val moneda: String?,
    val categoriaId: String?,
    val valorAnterior: String?,
    val valorNuevo: String?
)
