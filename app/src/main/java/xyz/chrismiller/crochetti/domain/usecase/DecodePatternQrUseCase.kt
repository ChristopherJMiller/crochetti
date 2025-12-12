package xyz.chrismiller.crochetti.domain.usecase

import xyz.chrismiller.crochetti.data.transfer.PatternTransferCodec
import xyz.chrismiller.crochetti.domain.model.Pattern
import xyz.chrismiller.crochetti.domain.model.transfer.PatternTransfer
import javax.inject.Inject

/**
 * Use case for decoding a pattern from QR code data.
 */
class DecodePatternQrUseCase @Inject constructor(
    private val codec: PatternTransferCodec
) {
    /**
     * Decode QR code data to a PatternTransfer (for preview).
     * @param rawValue The raw string value from the QR code
     * @return The decoded PatternTransfer, or an error
     */
    operator fun invoke(rawValue: String): Result<PatternTransfer> {
        return codec.decode(rawValue)
    }

    /**
     * Decode QR code data directly to a Pattern domain model.
     * @param rawValue The raw string value from the QR code
     * @return The decoded Pattern, or an error
     */
    fun toPattern(rawValue: String): Result<Pattern> {
        return codec.decodeToPattern(rawValue)
    }
}
