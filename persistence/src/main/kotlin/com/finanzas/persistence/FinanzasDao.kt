package com.finanzas.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

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

    @Update
    abstract suspend fun actualizarCuenta(cuenta: CuentaEntity)

    @Update
    abstract suspend fun actualizarCategoria(categoria: CategoriaEntity)

    @Update
    abstract suspend fun actualizarPresupuesto(presupuesto: PresupuestoEntity)

    @Delete
    abstract suspend fun eliminarCuenta(cuenta: CuentaEntity)

    @Delete
    abstract suspend fun eliminarCategoria(categoria: CategoriaEntity)

    @Delete
    abstract suspend fun eliminarPresupuesto(presupuesto: PresupuestoEntity)

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

    @Query("SELECT * FROM presupuestos WHERE id = :id LIMIT 1")
    abstract suspend fun obtenerPresupuesto(id: String): PresupuestoEntity?

    @Query("SELECT COUNT(*) FROM operaciones WHERE cuentaId = :id OR cuentaOrigenId = :id OR cuentaDestinoId = :id")
    abstract suspend fun contarOperacionesDeCuenta(id: String): Long

    @Query("SELECT COUNT(*) FROM presupuestos WHERE categoriaId = :id")
    abstract suspend fun contarPresupuestosDeCategoria(id: String): Long

    @Query("SELECT COUNT(*) FROM operaciones WHERE categoriaId = :id")
    abstract suspend fun contarOperacionesDeCategoria(id: String): Long
}
