package com.finanzas.persistence

import androidx.room.Database
import androidx.room.AutoMigration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

@Database(
    entities = [
        CuentaEntity::class,
        CategoriaEntity::class,
        PresupuestoEntity::class,
        OperacionEntity::class
    ],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
    exportSchema = true
)
abstract class FinanzasDatabase : RoomDatabase() {
    abstract fun finanzasDao(): FinanzasDao
}

object FinanzasDatabaseFactory {
    fun enMemoria(): FinanzasDatabase {
        return Room.inMemoryDatabaseBuilder<FinanzasDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    fun enArchivo(ruta: String): FinanzasDatabase {
        return Room.databaseBuilder<FinanzasDatabase>(ruta)
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
