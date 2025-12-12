package xyz.chrismiller.crochetti.domain.usecase

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.chrismiller.crochetti.data.transfer.PatternTransferCodec
import xyz.chrismiller.crochetti.domain.model.Pattern
import javax.inject.Inject

/**
 * Use case for encoding a pattern into a QR code bitmap.
 */
class EncodePatternQrUseCase @Inject constructor(
    private val codec: PatternTransferCodec
) {
    companion object {
        private const val QR_SIZE = 512
        private const val QR_MARGIN = 2
    }

    /**
     * Generate a QR code bitmap for the given pattern.
     * @param pattern The pattern to encode
     * @return A Bitmap containing the QR code, or an error
     */
    suspend operator fun invoke(pattern: Pattern): Result<Bitmap> = withContext(Dispatchers.Default) {
        codec.encodeChecked(pattern).mapCatching { encodedData ->
            generateQrBitmap(encodedData)
        }
    }

    /**
     * Generate a QR code bitmap with custom size.
     */
    suspend fun withSize(pattern: Pattern, size: Int): Result<Bitmap> = withContext(Dispatchers.Default) {
        codec.encodeChecked(pattern).mapCatching { encodedData ->
            generateQrBitmap(encodedData, size)
        }
    }

    private fun generateQrBitmap(data: String, size: Int = QR_SIZE): Bitmap {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to QR_MARGIN,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )

        val bitMatrix = QRCodeWriter().encode(
            data,
            BarcodeFormat.QR_CODE,
            size,
            size,
            hints
        )

        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        return Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }
}
