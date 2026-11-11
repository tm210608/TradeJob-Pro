package com.tradejob.pro.database.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tradejob.pro.database.data.dao.ClientDao
import com.tradejob.pro.database.data.dao.JobDao
import com.tradejob.pro.database.data.dao.JobPhotoDao
import com.tradejob.pro.database.data.dao.QuoteDao
import com.tradejob.pro.database.data.dao.QuoteItemDao
import com.tradejob.pro.database.data.dao.InvoiceDao
import com.tradejob.pro.database.data.dao.UserDao
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.database.data.entity.JobEntity
import com.tradejob.pro.database.data.entity.JobPhotoEntity
import com.tradejob.pro.database.data.entity.QuoteEntity
import com.tradejob.pro.database.data.entity.QuoteItemEntity
import com.tradejob.pro.database.data.entity.InvoiceEntity
import com.tradejob.pro.database.data.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ClientEntity::class,
        JobEntity::class,
        JobPhotoEntity::class,
        QuoteEntity::class,
        QuoteItemEntity::class,
        InvoiceEntity::class
    ],
    version = 7,
    exportSchema = true
)
abstract class TradeJobDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun clientDao(): ClientDao
    abstract fun jobDao(): JobDao
    abstract fun jobPhotoDao(): JobPhotoDao
    abstract fun quoteDao(): QuoteDao
    abstract fun quoteItemDao(): QuoteItemDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clients ADD COLUMN createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                db.execSQL("ALTER TABLE clients ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                db.execSQL("ALTER TABLE clients ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE jobs ADD COLUMN budgetAmount REAL")
                db.execSQL("ALTER TABLE jobs ADD COLUMN finalAmount REAL")
                db.execSQL("ALTER TABLE jobs ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE job_photos ADD COLUMN remoteUrl TEXT")
                db.execSQL("ALTER TABLE users ADD COLUMN firebaseUid TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS quotes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        clientId INTEGER NOT NULL,
                        jobId INTEGER,
                        number TEXT NOT NULL DEFAULT '',
                        status TEXT NOT NULL DEFAULT 'DRAFT',
                        vatRate REAL NOT NULL DEFAULT 21.0,
                        subtotal REAL NOT NULL DEFAULT 0.0,
                        vatAmount REAL NOT NULL DEFAULT 0.0,
                        total REAL NOT NULL DEFAULT 0.0,
                        notes TEXT,
                        validUntil INTEGER,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        isSynced INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(clientId) REFERENCES clients(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_quotes_clientId ON quotes(clientId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS quote_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        quoteId INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        quantity REAL NOT NULL DEFAULT 1.0,
                        unitPrice REAL NOT NULL DEFAULT 0.0,
                        total REAL NOT NULL DEFAULT 0.0,
                        sortOrder INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(quoteId) REFERENCES quotes(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_quote_items_quoteId ON quote_items(quoteId)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS invoices (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        clientId INTEGER NOT NULL,
                        quoteId INTEGER,
                        jobId INTEGER,
                        number TEXT NOT NULL DEFAULT '',
                        status TEXT NOT NULL DEFAULT 'PENDING',
                        paymentMethod TEXT,
                        vatRate REAL NOT NULL DEFAULT 21.0,
                        subtotal REAL NOT NULL DEFAULT 0.0,
                        vatAmount REAL NOT NULL DEFAULT 0.0,
                        irpfRate REAL,
                        total REAL NOT NULL DEFAULT 0.0,
                        paidAmount REAL NOT NULL DEFAULT 0.0,
                        issuedAt INTEGER NOT NULL DEFAULT 0,
                        dueAt INTEGER,
                        paidAt INTEGER,
                        notes TEXT,
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        isSynced INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(clientId) REFERENCES clients(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_invoices_clientId ON invoices(clientId)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN nif TEXT")
                db.execSQL("ALTER TABLE users ADD COLUMN businessName TEXT")
                db.execSQL("ALTER TABLE users ADD COLUMN businessAddress TEXT")
                db.execSQL("ALTER TABLE users ADD COLUMN businessCity TEXT")
                db.execSQL("ALTER TABLE users ADD COLUMN businessPhone TEXT")
                db.execSQL("ALTER TABLE users ADD COLUMN irpfRate REAL")
                db.execSQL("ALTER TABLE users ADD COLUMN verifactuEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE users ADD COLUMN invoiceSeries TEXT")
                db.execSQL("ALTER TABLE users ADD COLUMN invoiceNumber INTEGER NOT NULL DEFAULT 0")
            }
        }

        // SPEC-006: job_photos podía quedar sin isSynced/remoteUrl (u otras
        // columnas) en BDs antiguas — ninguna migración de la cadena las
        // añadía y la validación TableInfo crasheaba al abrir la app.
        // Rebuild con el esquema esperado copiando solo columnas presentes.
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val tableExists = db.query(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='job_photos'"
                ).use { it.moveToFirst() }

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS job_photos_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        jobId INTEGER NOT NULL,
                        photoPath TEXT NOT NULL,
                        description TEXT,
                        type TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        isSynced INTEGER NOT NULL,
                        remoteUrl TEXT,
                        FOREIGN KEY(jobId) REFERENCES jobs(id)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())

                if (tableExists) {
                    val existing = db.query("PRAGMA table_info(job_photos)").use { c ->
                        val nameIdx = c.getColumnIndex("name")
                        buildSet {
                            while (c.moveToNext()) add(c.getString(nameIdx))
                        }
                    }
                    fun col(name: String, fallback: String) =
                        if (name in existing) "`$name`" else "$fallback AS `$name`"
                    db.execSQL("""
                        INSERT INTO job_photos_new
                            (id, jobId, photoPath, description, type, createdAt, isSynced, remoteUrl)
                        SELECT ${col("id", "0")}, ${col("jobId", "0")},
                               ${col("photoPath", "''")}, ${col("description", "NULL")},
                               ${col("type", "'BEFORE'")}, ${col("createdAt", "0")},
                               ${col("isSynced", "0")}, ${col("remoteUrl", "NULL")}
                        FROM job_photos
                    """.trimIndent())
                    db.execSQL("DROP TABLE job_photos")
                }

                db.execSQL("ALTER TABLE job_photos_new RENAME TO job_photos")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_job_photos_jobId ON job_photos(jobId)")
            }
        }
    }
}
