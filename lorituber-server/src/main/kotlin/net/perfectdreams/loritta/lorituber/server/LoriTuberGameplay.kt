package net.perfectdreams.loritta.lorituber.server

fun main() {
    // For comparisons:
    // The Sims 1: one in game minute = one real life second
    // The Sims Online: one in game minute = five real life seconds
    val character = PlayableCharacter("Loritta Morenitta")
    var currentTime = 0

    while (true) {
        // One simulation minute is one second in real life
        val start = System.currentTimeMillis()
        val currentHour = currentTime / 60
        val currentMinutes = currentTime % 60
        println("Current Time: ${currentHour.toString().padStart(2, '0')}:${currentMinutes.toString().padStart(2, '0')}")

        println("Character Motives:")
        if (currentTime % 10 == 0) {
            character.hunger -= 1.0
        }

        println("Hunger: ${character.hunger}")
        currentTime++

        val end = System.currentTimeMillis()

        val diff = 1_000 - (end - start)
        println("Waiting ${diff}ms")

        Thread.sleep(diff)
    }
}

class PlayableCharacter(
    val name: String
) {
    var hunger = 100.0
}