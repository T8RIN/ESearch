package ru.tech.easysearch.application

import android.app.Application
import io.github.edsuns.adfilter.AdFilter
import ru.tech.easysearch.database.ESearchDatabase

class ESearchApplication : Application() {

    companion object {
        lateinit var database: ESearchDatabase
        var coeff = 0
    }

    override fun onCreate() {
        super.onCreate()

        AdFilter.create(this)
    }

}