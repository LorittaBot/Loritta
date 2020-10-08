package net.perfectdreams.akinatorreapi.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StepInformation(
    val question: String,
    val answers: List<String>,
    val step: Int,
    val progression: Double,
    @SerialName("questionid")
    val questionId: String,
    @SerialName("infogain")
    val infoGain: Double
)