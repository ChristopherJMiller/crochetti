package xyz.chrismiller.crochetti.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.chrismiller.crochetti.domain.model.StitchGroup
import xyz.chrismiller.crochetti.domain.model.StitchInstruction

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStitchGroupList(value: List<StitchGroup>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStitchGroupList(value: String): List<StitchGroup> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromLongSet(value: Set<Long>): String {
        return json.encodeToString(value.toList())
    }

    @TypeConverter
    fun toLongSet(value: String): Set<Long> {
        return try {
            json.decodeFromString<List<Long>>(value).toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    @TypeConverter
    fun fromLongList(value: List<Long>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStitchInstructionList(value: List<StitchInstruction>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toStitchInstructionList(value: String): List<StitchInstruction> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
