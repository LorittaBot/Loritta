package net.perfectdreams.loritta.lorituber.server

import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentVibes

fun main() {
    val vibes = LoriTuberVibes(0)

    vibes.rightVibes(LoriTuberVideoContentVibes.VIBE1)
    vibes.rightVibes(LoriTuberVideoContentVibes.VIBE2)
    vibes.rightVibes(LoriTuberVideoContentVibes.VIBE3)
    vibes.rightVibes(LoriTuberVideoContentVibes.VIBE4)
    vibes.rightVibes(LoriTuberVideoContentVibes.VIBE5)
    vibes.rightVibes(LoriTuberVideoContentVibes.VIBE6)
    vibes.rightVibes(LoriTuberVideoContentVibes.VIBE7)

    println(vibes.vibeType(LoriTuberVideoContentVibes.VIBE1))

    println(vibes.vibes)
}