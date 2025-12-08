package xyz.chrismiller.crochetti.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import xyz.chrismiller.crochetti.data.local.converter.Converters
import xyz.chrismiller.crochetti.data.local.dao.ComponentDao
import xyz.chrismiller.crochetti.data.local.dao.CustomStitchDao
import xyz.chrismiller.crochetti.data.local.dao.PatternDao
import xyz.chrismiller.crochetti.data.local.dao.ProjectDao
import xyz.chrismiller.crochetti.data.local.dao.RowDao
import xyz.chrismiller.crochetti.data.local.entity.ComponentEntity
import xyz.chrismiller.crochetti.data.local.entity.CustomStitchEntity
import xyz.chrismiller.crochetti.data.local.entity.PatternEntity
import xyz.chrismiller.crochetti.data.local.entity.ProjectEntity
import xyz.chrismiller.crochetti.data.local.entity.RowEntity

@Database(
    entities = [
        PatternEntity::class,
        ComponentEntity::class,
        RowEntity::class,
        ProjectEntity::class,
        CustomStitchEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CrochettiDatabase : RoomDatabase() {
    abstract fun patternDao(): PatternDao
    abstract fun componentDao(): ComponentDao
    abstract fun rowDao(): RowDao
    abstract fun projectDao(): ProjectDao
    abstract fun customStitchDao(): CustomStitchDao

    companion object {
        const val DATABASE_NAME = "crochetti.db"
    }
}
