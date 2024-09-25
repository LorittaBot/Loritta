package net.perfectdreams.loritta.lorituber

import kotlin.math.absoluteValue

fun main() {
    val vibe1Distance = (5 - -5).absoluteValue
    val vibe2Distance = (5 - -5).absoluteValue
    val vibe3Distance = (5 - -5).absoluteValue
    val vibe4Distance = (5 - -5).absoluteValue
    val vibe5Distance = (0 - 0).absoluteValue
    val vibe6Distance = (0 - 0).absoluteValue
    val vibe7Distance = (0 - 0).absoluteValue

    val vibe1Score = 10 - vibe1Distance
    val vibe2Score = 10 - vibe2Distance
    val vibe3Score = 10 - vibe3Distance
    val vibe4Score = 10 - vibe4Distance
    val vibe5Score = 10 - vibe5Distance
    val vibe6Score = 10 - vibe6Distance
    val vibe7Score = 10 - vibe7Distance

    val vibeScore = vibe1Score + vibe2Score + vibe3Score + vibe4Score + vibe5Score + vibe6Score + vibe7Score

    // The max vibe score is 70
    // With vibes, a vibe score of vibeScore>=50 is VERY good
    // While 20>vibeScore is VERY bad
    // Minimum vibe score is 30
    println("Min vibe score: $vibeScore")
}