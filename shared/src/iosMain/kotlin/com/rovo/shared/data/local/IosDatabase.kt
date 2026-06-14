package com.rovo.shared.data.local

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import platform.Foundation.NSHomeDirectory

class IosDBBuilder : DBBuilder {
    override fun build(): RovoDatabase {
        val dbFile = NSHomeDirectory() + "/Documents/rovo.db"
        return Room.databaseBuilder<RovoDatabase>(
            name = dbFile,
            factory = { RovoDatabase::class.instantiateImpl() }
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }
}
