package net.perfectdreams.loritta.lorituber.server

import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentVibes

fun main() {
    val vc = mutableMapOf<Int, Int>()

    repeat(1_000_000) {
        val vibes = LoriTuberVibes(0)

        val bools = listOf(false, true)

        val vibers = mutableMapOf<LoriTuberVideoContentCategory, LoriTuberSimpleSuperViewerData>()

        val randomVibes = LoriTuberVibes(0)
        randomVibes.setVibe(LoriTuberVideoContentVibes.VIBE1, bools.random())
        randomVibes.setVibe(LoriTuberVideoContentVibes.VIBE2, bools.random())
        randomVibes.setVibe(LoriTuberVideoContentVibes.VIBE3, bools.random())
        randomVibes.setVibe(LoriTuberVideoContentVibes.VIBE4, bools.random())
        randomVibes.setVibe(LoriTuberVideoContentVibes.VIBE5, bools.random())
        randomVibes.setVibe(LoriTuberVideoContentVibes.VIBE6, bools.random())
        randomVibes.setVibe(LoriTuberVideoContentVibes.VIBE7, bools.random())

        var matchedVibes = 0
        for (vibe in LoriTuberVideoContentVibes.entries) {
            if (randomVibes.vibeType(vibe) == vibes.vibeType(vibe)) {
                matchedVibes++
            }
        }
        // println("Matched vibes: $matchedVibes")
        vc[matchedVibes] = vc.getOrPut(matchedVibes) { 0 } + 1
    }

    val total = vc.values.sum()

    vc.forEach { t, u ->
        println("$t: $u (${(u / total.toDouble()).toDouble() * 100}%)")
    }
}