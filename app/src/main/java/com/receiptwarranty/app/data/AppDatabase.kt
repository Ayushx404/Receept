package com.receiptwarranty.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ReceiptWarranty::class, DeletedItem::class], version = 16, exportSchema = false)
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
                            MIGRATION_6_7,
                            MIGRATION_7_8,
                            MIGRATION_8_9,
                            MIGRATION_9_10,
                            MIGRATION_10_11,
                            MIGRATION_11_12,
                            MIGRATION_12_13,
                            MIGRATION_13_14
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
                            MIGRATION_6_7,
                            MIGRATION_7_8,
                            MIGRATION_8_9,
                            MIGRATION_13_14
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

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns
                database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN price REAL")
                database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN tags TEXT")
                database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN additionalImageUris TEXT")
                database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN deletedAt INTEGER")
                // Drop all indexes that are NOT declared in the @Entity annotation.
                // Room validates the schema strictly — any extra indexes cause a crash.
                database.execSQL("DROP INDEX IF EXISTS idx_is_deleted")
                database.execSQL("DROP INDEX IF EXISTS idx_category")
                database.execSQL("DROP INDEX IF EXISTS idx_type")
                database.execSQL("DROP INDEX IF EXISTS idx_warranty_expiry")
            }
        }
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop stray indexes from receipt_warranty that Room does NOT declare.
                // These cause schema-validation crashes on upgrades.
                database.execSQL("DROP INDEX IF EXISTS idx_is_deleted")
                database.execSQL("DROP INDEX IF EXISTS idx_category")
                database.execSQL("DROP INDEX IF EXISTS idx_type")
                database.execSQL("DROP INDEX IF EXISTS idx_warranty_expiry")

                // The deleted_items @Entity DOES declare idx_deleted_user — ensure it exists.
                database.execSQL("CREATE INDEX IF NOT EXISTS idx_deleted_user ON deleted_items(userId)")
            }
        }
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No schema changes — BILL is a new valid string for the 'type' column.
                // Room requires a version bump when the entity's enum changes.
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN customReminderDays INTEGER")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No schema changes — SUBSCRIPTION is a new valid string for the 'type' column.
                // Room requires a version bump when the entity's enum changes.
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN isPaid INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN lastPaidDate INTEGER")
                database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN billingCycle TEXT")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Drop company column by recreating table (SQLite doesn't support DROP COLUMN directly)
                // First add paymentHistory column if it doesn't exist
                try {
                    database.execSQL("ALTER TABLE receipt_warranty ADD COLUMN paymentHistory TEXT")
                } catch (e: Exception) {
                    // Column may already exist, ignore
                }
                
                database.execSQL("""
                    CREATE TABLE receipt_warranty_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        category TEXT,
                        imageUri TEXT,
                        driveFileId TEXT,
                        cloudId TEXT,
                        purchaseDate INTEGER,
                        warrantyExpiryDate INTEGER,
                        reminderDays TEXT,
                        customReminderDays INTEGER,
                        notes TEXT,
                        price REAL,
                        tags TEXT,
                        additionalImageUris TEXT,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        deletedAt INTEGER,
                        isPaid INTEGER NOT NULL DEFAULT 0,
                        lastPaidDate INTEGER,
                        billingCycle TEXT,
                        paymentHistory TEXT,
                        isArchived INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO receipt_warranty_new (
                        id, type, title, category, imageUri, driveFileId, cloudId,
                        purchaseDate, warrantyExpiryDate, reminderDays, customReminderDays,
                        notes, price, tags, additionalImageUris, isDeleted, deletedAt,
                        isPaid, lastPaidDate, billingCycle, isArchived,
                        createdAt, updatedAt
                    )
                    SELECT
                        id, type, title, category, imageUri, driveFileId, cloudId,
                        purchaseDate, warrantyExpiryDate, reminderDays, customReminderDays,
                        notes, price, tags, additionalImageUris, isDeleted, deletedAt,
                        isPaid, lastPaidDate, billingCycle, isArchived,
                        createdAt, updatedAt
                    FROM receipt_warranty
                """.trimIndent())
                database.execSQL("DROP TABLE receipt_warranty")
                database.execSQL("ALTER TABLE receipt_warranty_new RENAME TO receipt_warranty")
            }
        }
    }
}
