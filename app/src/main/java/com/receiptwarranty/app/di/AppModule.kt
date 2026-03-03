package com.receiptwarranty.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import com.receiptwarranty.app.data.AppDatabase
import com.receiptwarranty.app.data.ReceiptWarrantyDao
import com.receiptwarranty.app.data.ReceiptWarrantyRepository
import com.receiptwarranty.app.workers.WarrantyReminderScheduler
import com.receiptwarranty.app.util.ConnectivityObserver
import com.receiptwarranty.app.data.AppearancePreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideReceiptWarrantyDao(database: AppDatabase): ReceiptWarrantyDao {
        return database.receiptWarrantyDao()
    }

    @Provides
    @Singleton
    fun provideReceiptWarrantyRepository(dao: ReceiptWarrantyDao): ReceiptWarrantyRepository {
        return ReceiptWarrantyRepository(dao)
    }

    @Provides
    @Singleton
    fun provideWarrantyReminderScheduler(@ApplicationContext context: Context): WarrantyReminderScheduler {
        return WarrantyReminderScheduler(context)
    }

    @Provides
    @Singleton
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver {
        return ConnectivityObserver(context)
    }

}
