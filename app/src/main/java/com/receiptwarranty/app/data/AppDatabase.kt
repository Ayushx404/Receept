package com.receiptwarranty.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ReceiptWarranty::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptWarrantyDao(): ReceiptWarrantyDao

    companion object {
        const val DATABASE_NAME = "receipt_warranty_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        DATABASE_NAME
                    )
                        .addMigrations(MIGRATION_1_2)
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    context.deleteDatabase(DATABASE_NAME)
                    INSTANCE ?: synchronized(this) {
                        val instance = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            DATABASE_NAME
                        )
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration()
                            .build()
                        INSTANCE = instance
                        instance
                    }
                }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE receipt_warranty_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        type TEXT NOT NULL CHECK(type IN ('RECEIPT', 'WARRANTY')),
                        title TEXT NOT NULL,
                        company TEXT NOT NULL,
                        category TEXT,
                        imageUri TEXT,
                        purchaseDate INTEGER,
                        warrantyExpiryDate INTEGER,
                        reminderDays INTEGER,
                        notes TEXT,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    INSERT INTO receipt_warranty_new (
                        id, type, title, company, category, imageUri,
                        purchaseDate, warrantyExpiryDate, reminderDays, notes, createdAt
                    )
                    SELECT
                        id,
                        'WARRANTY' as type,
                        title,
                        company,
                        NULL as category,
                        COALESCE(receiptImageUri, warrantyImageUri) as imageUri,
                        purchaseDate,
                        warrantyExpiryDate,
                        NULL as reminderDays,
                        notes,
                        createdAt
                    FROM receipt_warranty
                """.trimIndent())

                database.execSQL("DROP TABLE receipt_warranty")
                database.execSQL("ALTER TABLE receipt_warranty_new RENAME TO receipt_warranty")

                database.execSQL("CREATE INDEX IF NOT EXISTS idx_category ON receipt_warranty(category)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_type ON receipt_warranty(type)")
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_warranty_expiry ON receipt_warranty(warrantyExpiryDate)")
            }
        }
    }
}
