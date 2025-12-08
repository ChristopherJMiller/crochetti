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
import xyz.chrismiller.crochetti.data.local.dao.PatternDao
import xyz.chrismiller.crochetti.data.local.dao.ProgressDao
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
            .addMigrations(MIGRATION_1_2)
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
    fun provideProgressDao(database: CrochettiDatabase): ProgressDao {
        return database.progressDao()
    }
}
