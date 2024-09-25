package net.perfectdreams.loritta.lorituber

import java.util.*

fun main() {
    val viewers = mutableListOf<MiniViewer>()

    repeat(100) {
        viewers.add(
            MiniViewer(
                "User$it",
                0,
                Random().nextInt(1_440, 10_081),
                if (Random().nextBoolean())
                    1
                else
                    -1,
                if (Random().nextBoolean())
                    1
                else
                    -1,
                if (Random().nextBoolean())
                    1
                else
                    -1,
                if (Random().nextBoolean())
                    1
                else
                    -1,
                if (Random().nextBoolean())
                    1
                else
                    -1,
                if (Random().nextBoolean())
                    1
                else
                    -1,
                if (Random().nextBoolean())
                    1
                else
                    -1,
            )
        )
    }

    val initial = viewers.first().copy()

    val firstVibers = mutableListOf<String>()
    val video = MiniVideo(
        1,
        1,
        -1,
        1,
        1,
        -1,
        -1
    )

    var t = 0
    var maxVibesAvg = 0.0
    var minimumVibesAvg = Double.MAX_VALUE

    while (true) {
        val vibeScores = mutableListOf<Int>()
        var vibes = 0
        var neutral = 0
        var doNotVibe = 0
        val currentVibers = mutableListOf<String>()

        for (viewer in viewers) {
            var vibeScore = 0

            if (viewer.vibes1 == video.vibes1)
                vibeScore++
            if (viewer.vibes2 == video.vibes2)
                vibeScore++
            if (viewer.vibes3 == video.vibes3)
                vibeScore++
            if (viewer.vibes4 == video.vibes4)
                vibeScore++
            if (viewer.vibes5 == video.vibes5)
                vibeScore++
            if (viewer.vibes6 == video.vibes6)
                vibeScore++
            if (viewer.vibes7 == video.vibes7)
                vibeScore++
            vibeScores.add(vibeScore)

            if (vibeScore >= 5) {
                // 5, 6, 7
                vibes++

                if (t == 0) {
                    firstVibers.add(viewer.name)
                }

                currentVibers.add(viewer.name)
            } else if (1 >= vibeScore) {
                // 0, 1
                doNotVibe++
            } else {
                // 2, 3, 4
                neutral++
            }
        }

        val s = (t / 20_160).toLong()
        if (t % 20_160 == 0) {
            println("${t / 1440} in-game days (${(t / 86400)} real life days)")
            println("[$t] $initial")
            println("[$t] ${viewers.first()}")
            println("[$t] First vibers: $firstVibers")
            println("[$t] Current vibers: $currentVibers")
            println("[$t] Seed: $s; Vibe Scores: ${vibeScores.average()}; Vibes: $vibes; Does not Vibe: $doNotVibe; Neutral: $neutral")
            println("[$t] Max vibes: $maxVibesAvg; Min vibes: $minimumVibesAvg")
            if (t >= (525_960 * 5))
                return
        }

        if (vibeScores.average() > maxVibesAvg) {
            maxVibesAvg = vibeScores.average()
        }

        if (minimumVibesAvg > vibeScores.average()) {
            minimumVibesAvg = vibeScores.average()
        }

        if (t != 0) {
            // Attempt to converge viewers to -1
            for (viewer in viewers) {
                if (t.toLong() - viewer.lastTrendChange > viewer.influencedByTrendsAfterTicks) {

                    viewer.lastTrendChange = t.toLong()
                    val y = Random().nextInt(0, 7)
                    val rand = Random(s)
                    val vibes1Ch = rand.nextBoolean()
                    val vibes2Ch = rand.nextBoolean()
                    val vibes3Ch = rand.nextBoolean()
                    val vibes4Ch = rand.nextBoolean()
                    val vibes5Ch = rand.nextBoolean()
                    val vibes6Ch = rand.nextBoolean()
                    val vibes7Ch = rand.nextBoolean()

                    if (y == 0) {
                        viewer.vibes1 = if (vibes1Ch)
                            1
                        else
                            -1
                    }
                    if (y == 1) {
                        viewer.vibes2 = if (vibes2Ch)
                            1
                        else
                            -1
                    }
                    if (y == 2) {
                        viewer.vibes3 = if (vibes3Ch)
                            1
                        else
                            -1
                    }
                    if (y == 3) {
                        viewer.vibes4 = if (vibes4Ch)
                            1
                        else
                            -1
                    }
                    if (y == 4) {
                        viewer.vibes5 = if (vibes5Ch)
                            1
                        else
                            -1
                    }
                    if (y == 5) {
                        viewer.vibes6 = if (vibes6Ch)
                            1
                        else
                            -1
                    }
                    if (y == 6) {
                        viewer.vibes7 = if (vibes7Ch)
                            1
                        else
                            -1
                    }
                }
            }
        }

        t++
    }
}

data class MiniViewer(
    val name: String,
    var lastTrendChange: Long,
    val influencedByTrendsAfterTicks: Int,
    var vibes1: Int,
    var vibes2: Int,
    var vibes3: Int,
    var vibes4: Int,
    var vibes5: Int,
    var vibes6: Int,
    var vibes7: Int
)

data class MiniVideo(
    val vibes1: Int,
    val vibes2: Int,
    val vibes3: Int,
    val vibes4: Int,
    val vibes5: Int,
    val vibes6: Int,
    val vibes7: Int
)