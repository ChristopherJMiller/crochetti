package xyz.chrismiller.crochetti.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Indicates which side of the work a row is worked on.
 */
@Serializable
enum class Sided(val display: String, val abbreviation: String) {
    @SerialName("RS")
    RightSide("Right Side", "RS"),

    @SerialName("WS")
    WrongSide("Wrong Side", "WS");

    companion object {
        fun fromString(value: String?): Sided? {
            return when (value?.uppercase()?.trim()) {
                "RS", "RIGHT", "RIGHT SIDE" -> RightSide
                "WS", "WRONG", "WRONG SIDE" -> WrongSide
                else -> null
            }
        }
    }
}
