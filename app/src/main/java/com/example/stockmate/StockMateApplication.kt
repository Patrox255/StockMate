package com.example.stockmate

import android.app.Application
import com.example.stockmate.data.util.img.OrphanedProductImagesCleaner
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class StockMateApplication : Application() {
    @Inject
    lateinit var orphanedProductImagesCleaner: OrphanedProductImagesCleaner

    override fun onCreate() {
        super.onCreate()

        // Upon launch, we want to clean up any product images that are no longer used by any product in the database.
        // This is to prevent orphaned images from taking up space on the device.
        CoroutineScope(Dispatchers.IO).launch {
            orphanedProductImagesCleaner.cleanUp()
        }
    }
}