package ru.tech.easysearch.application

import android.app.Application
import ru.tech.easysearch.database.ESearchDatabase

class ESearchApplication : Application() {

    companion object {
        lateinit var database: ESearchDatabase
        var coeff = 0
    }

}