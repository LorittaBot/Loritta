package net.perfectdreams.loritta.lorituber

import java.util.*
import kotlin.time.measureTime

fun main() {
    val videos = mutableListOf<LoriTuberVideoTest>()

    repeat(1_000_000) {
        videos.add(
            LoriTuberVideoTest(
                UUID.randomUUID().toString(),
                0,
                0,
                0,
            )
        )
    }

    repeat(1_000_000) {
        val d = measureTime {
            for (video in videos) {
                video.views++
                video.likes++
                video.dislikes++
            }
        }

        println("Time: $d")
    }
}

data class LoriTuberVideoTest(
    val title: String,
    var views: Int,
    var likes: Int,
    var dislikes: Int,
)