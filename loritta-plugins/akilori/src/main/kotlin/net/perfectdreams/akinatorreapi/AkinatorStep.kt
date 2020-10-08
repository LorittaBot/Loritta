package net.perfectdreams.akinatorreapi

open class AkinatorStep(
    val question: String,
    val answers: List<String>,
    val step: Int,
    val progression: Double,
    val questionId: Int,
    val infoGain: Double
)