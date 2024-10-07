package net.perfectdreams.loritta.lorituber.server

import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentVibes

fun main() {
    val wrapper = mutableListOf<ViewerWrapper>()
    val possibleVibes = mutableListOf<LoriTuberVibes>()
    val bools = listOf(false, true)

    repeat(LoriTuberVideoContentCategory.entries.size * 256) {
        val categories = LoriTuberVideoContentCategory.entries.shuffled().take(3)

        if (wrapper.any { it.category.containsAll(categories) && categories.containsAll(it.category) }) {
            return@repeat
            println("Whoopsie")
            System.exit(0)
        }

        for (vibe1 in bools) {
            for (vibe2 in bools) {
                for (vibe3 in bools) {
                    for (vibe4 in bools) {
                        for (vibe5 in bools) {
                            for (vibe6 in bools) {
                                for (vibe7 in bools) {
                                    val vibes = LoriTuberVibes(0)
                                    vibes.setVibe(LoriTuberVideoContentVibes.VIBE1, vibe1)
                                    vibes.setVibe(LoriTuberVideoContentVibes.VIBE2, vibe2)
                                    vibes.setVibe(LoriTuberVideoContentVibes.VIBE3, vibe3)
                                    vibes.setVibe(LoriTuberVideoContentVibes.VIBE4, vibe4)
                                    vibes.setVibe(LoriTuberVideoContentVibes.VIBE5, vibe5)
                                    vibes.setVibe(LoriTuberVideoContentVibes.VIBE6, vibe6)
                                    // vibes.setVibe(LoriTuberVideoContentVibes.VIBE7, vibe7)
                                    possibleVibes.add(vibes)

                                    wrapper.add(
                                        ViewerWrapper(
                                            categories,
                                            vibes
                                        ).also { println(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    println(possibleVibes.size)
    println(wrapper.size)
}

data class ViewerWrapper(
    val category: List<LoriTuberVideoContentCategory>,
    val vibes: LoriTuberVibes
)