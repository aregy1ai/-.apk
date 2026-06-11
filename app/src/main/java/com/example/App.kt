package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.AppDatabase

class App : Application() {
    lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDatabase::class.java, "downloads.db").build()
    }
}
