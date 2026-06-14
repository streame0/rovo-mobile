package com.rovo.shared.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

class AndroidDBBuilder(private val context: Context) : DBBuilder {
    override fun build(): RovoDatabase {
        val dbFile = context.getDatabasePath("rovo.db")
        return Room.databaseBuilder<RovoDatabase>(
            context = context,
            name = dbFile.absolutePath
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
