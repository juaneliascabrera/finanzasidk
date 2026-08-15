package com.finanzas.app

import android.app.Application
import androidx.room.Room
import com.finanzas.persistence.FinanzasDatabase
import com.finanzas.persistence.FinanzasRepository

class FinanzasApplication : Application() {
    val repository: FinanzasRepository by lazy {
        FinanzasRepository(
            Room.databaseBuilder(this, FinanzasDatabase::class.java, "finanzas.db").build()
        )
    }

    override fun onTerminate() {
        repository.cerrar()
        super.onTerminate()
    }
}
