package xyz.chrismiller.crochetti.data.transfer

import android.util.Base64
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.chrismiller.crochetti.domain.model.Pattern
import xyz.chrismiller.crochetti.domain.model.transfer.PatternTransfer
import xyz.chrismiller.crochetti.domain.model.transfer.toPattern
import xyz.chrismiller.crochetti.domain.model.transfer.toTransfer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Error types for QR code operations.
 */
sealed class QrError : Exception() {
    data class PatternTooLarge(
        val actualSize: Int,
        val maxSize: Int
    ) : QrError() {
        override val message = "Pattern is too large for QR code ($actualSize bytes, max $maxSize)"
    }

    data class UnsupportedVersion(
        val foundVersion: Int,
        val supportedVersion: Int
    ) : QrError() {
        override val message = "QR code requires app version $foundVersion, current supports up to $supportedVersion"
    }

    data object InvalidFormat : QrError() {
        override val message = "This QR code does not contain a valid Crochetti pattern"
    }

    data object DecompressionFailed : QrError() {
        override val message = "Failed to decompress pattern data"
    }

    data class ValidationFailed(override val message: String) : QrError()
}

/**
 * Codec for encoding/decoding patterns for QR code transfer.
 *
 * Encoding pipeline: Pattern -> PatternTransfer -> JSON -> GZIP -> Base64 (URL-safe)
 * Decoding pipeline: Base64 -> GZIP decompress -> JSON -> PatternTransfer -> Pattern
 */
@Singleton
class PatternTransferCodec @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true  // Forward compatibility
        encodeDefaults = false    // Smaller output - skip default values
    }

    companion object {
        /** Current version of the transfer format */
        const val CURRENT_VERSION = 1

        /** Maximum supported version for decoding */
        const val MAX_SUPPORTED_VERSION = 1

        /** Maximum payload size for QR code (QR version 40, EC level M) */
        const val MAX_QR_CAPACITY = 2953
    }

    /**
     * Encode a pattern for QR code transfer.
     * @return Base64-encoded, GZIP-compressed JSON string
     */
    fun encode(pattern: Pattern): Result<String> = runCatching {
        val transfer = pattern.toTransfer()
        val jsonString = json.encodeToString(transfer)
        val compressed = jsonString.gzip()
        Base64.encodeToString(compressed, Base64.URL_SAFE or Base64.NO_WRAP)
    }

    /**
     * Encode a pattern and check if it fits in a QR code.
     * @return Encoded string if it fits, or error if too large
     */
    fun encodeChecked(pattern: Pattern): Result<String> {
        return encode(pattern).mapCatching { encoded ->
            if (encoded.length > MAX_QR_CAPACITY) {
                throw QrError.PatternTooLarge(
                    actualSize = encoded.length,
                    maxSize = MAX_QR_CAPACITY
                )
            }
            encoded
        }
    }

    /**
     * Decode a pattern from QR code data.
     * @param data Base64-encoded, GZIP-compressed JSON string
     * @return The decoded PatternTransfer
     */
    fun decode(data: String): Result<PatternTransfer> = runCatching {
        val compressed = try {
            Base64.decode(data, Base64.URL_SAFE)
        } catch (e: IllegalArgumentException) {
            throw QrError.InvalidFormat
        }

        val jsonString = try {
            compressed.gunzip()
        } catch (e: Exception) {
            throw QrError.DecompressionFailed
        }

        val transfer = try {
            json.decodeFromString<PatternTransfer>(jsonString)
        } catch (e: Exception) {
            throw QrError.InvalidFormat
        }

        // Version check
        if (transfer.version > MAX_SUPPORTED_VERSION) {
            throw QrError.UnsupportedVersion(
                foundVersion = transfer.version,
                supportedVersion = MAX_SUPPORTED_VERSION
            )
        }

        // Validate required fields
        if (transfer.name.isBlank()) {
            throw QrError.ValidationFailed("Pattern name is required")
        }
        if (transfer.components.isEmpty()) {
            throw QrError.ValidationFailed("Pattern must have at least one component")
        }

        transfer
    }

    /**
     * Decode and convert to a Pattern domain model.
     */
    fun decodeToPattern(data: String): Result<Pattern> {
        return decode(data).map { it.toPattern() }
    }

    /**
     * Get the encoded size for a pattern without checking limits.
     */
    fun getEncodedSize(pattern: Pattern): Int {
        return encode(pattern).getOrNull()?.length ?: -1
    }
}

/**
 * GZIP compress a string to bytes.
 */
private fun String.gzip(): ByteArray {
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).use { gzip ->
        gzip.write(this.toByteArray(Charsets.UTF_8))
    }
    return bos.toByteArray()
}

/**
 * GZIP decompress bytes to a string.
 */
private fun ByteArray.gunzip(): String {
    val bis = ByteArrayInputStream(this)
    GZIPInputStream(bis).use { gzip ->
        return gzip.bufferedReader(Charsets.UTF_8).readText()
    }
}
