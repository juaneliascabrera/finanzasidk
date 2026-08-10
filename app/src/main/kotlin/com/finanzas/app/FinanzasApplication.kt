package com.finanzas.app

import android.app.Application
import com.finanzas.persistence.FinanzasDatabaseFactory
import com.finanzas.persistence.FinanzasRepository

class FinanzasApplication : Application() {
    val repository: FinanzasRepository by lazy {
        FinanzasRepository(
            FinanzasDatabaseFactory.enArchivo("$filesDir/finanzas.db")
        )
    }

    override fun onTerminate() {
        repository.cerrar()
        super.onTerminate()
    }
}
