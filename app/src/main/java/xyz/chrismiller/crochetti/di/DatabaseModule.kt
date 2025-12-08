package xyz.chrismiller.crochetti.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import xyz.chrismiller.crochetti.data.local.CrochettiDatabase
import xyz.chrismiller.crochetti.data.local.dao.ComponentDao
import xyz.chrismiller.crochetti.data.local.dao.CustomStitchDao
import xyz.chrismiller.crochetti.data.local.dao.PatternDao
import xyz.chrismiller.crochetti.data.local.dao.ProjectDao
import xyz.chrismiller.crochetti.data.local.dao.RowDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE rows ADD COLUMN hasMagicRing INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS custom_stitches (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    patternId INTEGER NOT NULL,
                    abbreviation TEXT NOT NULL,
                    displayName TEXT NOT NULL,
                    description TEXT NOT NULL DEFAULT '',
                    instructionsJson TEXT NOT NULL,
                    stitchCount INTEGER NOT NULL,
                    rawDsl TEXT NOT NULL,
                    sortOrder INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY(patternId) REFERENCES patterns(id) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_custom_stitches_patternId ON custom_stitches(patternId)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_custom_stitches_patternId_abbreviation ON custom_stitches(patternId, abbreviation)")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add repeatCount column to rows table for repeat row support (e.g., "7-9)")
            db.execSQL("ALTER TABLE rows ADD COLUMN repeatCount INTEGER NOT NULL DEFAULT 1")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create projects table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS projects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    patternId INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'active',
                    currentComponentId INTEGER,
                    currentRowIndex INTEGER NOT NULL DEFAULT 0,
                    currentStitchCount INTEGER NOT NULL DEFAULT 0,
                    completedRowIdsJson TEXT NOT NULL DEFAULT '[]',
                    createdAt INTEGER NOT NULL,
                    lastUpdated INTEGER NOT NULL,
                    completedAt INTEGER,
                    FOREIGN KEY(patternId) REFERENCES patterns(id) ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS index_projects_patternId ON projects(patternId)")

            // Migrate existing progress data to projects
            db.execSQL("""
                INSERT INTO projects (patternId, name, status, currentComponentId, currentRowIndex,
                                      currentStitchCount, completedRowIdsJson, createdAt, lastUpdated, completedAt)
                SELECT
                    p.patternId,
                    patterns.name,
                    'active',
                    p.currentComponentId,
                    p.currentRowIndex,
                    p.currentStitchCount,
                    p.completedRowIdsJson,
                    p.lastUpdated,
                    p.lastUpdated,
                    NULL
                FROM progress p
                INNER JOIN patterns ON patterns.id = p.patternId
            """.trimIndent())

            // Drop old progress table
            db.execSQL("DROP TABLE IF EXISTS progress")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): CrochettiDatabase {
        return Room.databaseBuilder(
            context,
            CrochettiDatabase::class.java,
            CrochettiDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
    }

    @Provides
    fun providePatternDao(database: CrochettiDatabase): PatternDao {
        return database.patternDao()
    }

    @Provides
    fun provideComponentDao(database: CrochettiDatabase): ComponentDao {
        return database.componentDao()
    }

    @Provides
    fun provideRowDao(database: CrochettiDatabase): RowDao {
        return database.rowDao()
    }

    @Provides
    fun provideProjectDao(database: CrochettiDatabase): ProjectDao {
        return database.projectDao()
    }

    @Provides
    fun provideCustomStitchDao(database: CrochettiDatabase): CustomStitchDao {
        return database.customStitchDao()
    }
}
