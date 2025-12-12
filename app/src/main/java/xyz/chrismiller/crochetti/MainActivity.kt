package xyz.chrismiller.crochetti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import xyz.chrismiller.crochetti.data.repository.PatternRepository
import xyz.chrismiller.crochetti.domain.usecase.DecodePatternQrUseCase
import xyz.chrismiller.crochetti.domain.usecase.EncodePatternQrUseCase
import xyz.chrismiller.crochetti.ui.navigation.CrochettiNavHost
import xyz.chrismiller.crochetti.ui.theme.CrochettiTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var encodePatternQrUseCase: EncodePatternQrUseCase

    @Inject
    lateinit var decodePatternQrUseCase: DecodePatternQrUseCase

    @Inject
    lateinit var patternRepository: PatternRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CrochettiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CrochettiNavHost(
                        encodePatternQrUseCase = encodePatternQrUseCase,
                        decodePatternQrUseCase = decodePatternQrUseCase,
                        patternRepository = patternRepository
                    )
                }
            }
        }
    }
}
