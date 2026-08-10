package com.finanzas.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
abstract class FinanzasDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertarCuenta(cuenta: CuentaEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertarCategoria(categoria: CategoriaEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertarPresupuesto(presupuesto: PresupuestoEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertarOperacion(operacion: OperacionEntity): Long

    @Query("SELECT * FROM cuentas ORDER BY id")
    abstract suspend fun obtenerCuentas(): List<CuentaEntity>

    @Query("SELECT * FROM categorias ORDER BY id")
    abstract suspend fun obtenerCategorias(): List<CategoriaEntity>

    @Query("SELECT * FROM presupuestos ORDER BY id")
    abstract suspend fun obtenerPresupuestos(): List<PresupuestoEntity>

    @Query("SELECT * FROM operaciones ORDER BY ordenRegistro")
    abstract suspend fun obtenerOperaciones(): List<OperacionEntity>

    @Query("SELECT * FROM cuentas WHERE id = :id LIMIT 1")
    abstract suspend fun obtenerCuenta(id: String): CuentaEntity?

    @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
    abstract suspend fun obtenerCategoria(id: String): CategoriaEntity?
}
