package com.example.stockmate.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.stockmate.data.AppDatabase
import com.example.stockmate.data.dao.ProductDao
import com.example.stockmate.data.dao.ProductMultiplierDao
import com.example.stockmate.data.dao.StockLogDao
import com.example.stockmate.data.repository.ProductRepository
import com.example.stockmate.data.util.ImageStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "stockmate_db"
        )
            // ONLY FOR DEVELOPMENT PURPOSES, DELETE LATER!!!
            .fallbackToDestructiveMigration()
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @Provides
    fun provideProductDao(db: AppDatabase) = db.productDao()
    @Provides
    fun provideProductMultiplierDao(db: AppDatabase) = db.productMultiplierDao()
    @Provides
    fun provideStockLogDao(db: AppDatabase) = db.stockLogDao()

    @Provides
    @Singleton
    fun provideRepository(
        db: AppDatabase,
        productDao: ProductDao,
        productMultiplierDao: ProductMultiplierDao,
        stockLogDao: StockLogDao
    ) = ProductRepository(
        db,
        productDao,
        productMultiplierDao,
        stockLogDao
    )

    @Provides
    @Singleton
    fun provideImageStorage(
        @ApplicationContext context: Context
    ): ImageStorage = ImageStorage(context)
}