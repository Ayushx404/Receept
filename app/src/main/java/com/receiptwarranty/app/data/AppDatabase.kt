package com.receiptwarranty.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ReceiptWarranty::class, DeletedItem::class], version = 7, exportSchema = false)
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
                        .addMigrations(
                            MIGRATION_1_2,
                            MIGRATION_2_3,
                            MIGRATION_3_4,
                            MIGRATION_4_5,
                            MIGRATION_5_6,
                            MIGRATION_6_7
                        )
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
                        .addMigrations(
                            MIGRATION_1_2,
                            MIGRATION_2_3,
                            MIGRATION_3_4,
                            MIGRATION_4_5,
                            MIGRATION_5_6,
                            MIGRATION_6_7
                        )
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN driveFileId TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN cloudId TEXT")
            }
        }
        
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS deleted_items (
                        cloudId TEXT NOT NULL,
                        deletedAt INTEGER NOT NULL,
                        userId TEXT NOT NULL,
                        PRIMARY KEY (cloudId, userId)
                    )
                """.trimIndent())
                
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_deleted_user ON deleted_items(userId)")
                
                // Add updatedAt column to receipt_warranty table
                database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
            }
        }
        
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop old table if exists and recreate with correct schema
                database.execSQL("DROP TABLE IF EXISTS deleted_items")
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS deleted_items (
                        cloudId TEXT NOT NULL,
                        deletedAt INTEGER NOT NULL,
                        userId TEXT NOT NULL,
                        PRIMARY KEY (cloudId, userId)
                    )
                """.trimIndent())
                
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_deleted_user ON deleted_items(userId)")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_deleted_user ON deleted_items(userId)")
            }
        }
    }
}
