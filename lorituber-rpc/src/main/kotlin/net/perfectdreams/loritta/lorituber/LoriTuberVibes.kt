package net.perfectdreams.loritta.lorituber

import kotlinx.serialization.Serializable

@Serializable
data class LoriTuberVibes(var vibes: Long) {
    companion object {
        fun vibeMatches(matchFrom: LoriTuberVibes, matchTo: LoriTuberVibes): Int {
            var matchedVibes = 0
            for (vibe in LoriTuberVideoContentVibes.entries) {
                if (matchFrom.vibeType(vibe) == matchTo.vibeType(vibe)) {
                    matchedVibes++
                }
            }
            return matchedVibes
        }
    }

    fun setVibe(vibe: LoriTuberVideoContentVibes, value: Boolean) {
        if (value)
            rightVibes(vibe)
        else
            leftVibes(vibe)
    }

    fun rightVibes(vibe: LoriTuberVideoContentVibes) {
        this.vibes = vibes or (1L shl vibe.ordinal)
    }

    fun leftVibes(vibe: LoriTuberVideoContentVibes) {
        this.vibes = vibes and (1L shl vibe.ordinal).inv()
    }

    fun vibeType(vibe: LoriTuberVideoContentVibes): Boolean {
        return (vibes and (1L shl vibe.ordinal)) != 0L
    }
}