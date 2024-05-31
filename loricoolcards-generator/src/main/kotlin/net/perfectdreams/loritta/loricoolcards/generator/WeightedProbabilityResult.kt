package net.perfectdreams.loritta.loricoolcards.generator

fun main() {
    val cardWeights = (1..510).associate {
        "Card #${it}" to 1.0
    }

    // Example: Calculate the chance of Card #0001 being picked
    val totalWeight = cardWeights.values.sum()
    val chanceOfCard1 = cardWeights["Card #1"]!! / totalWeight * 100

    println("Card #0001 has ${String.format("%.2f", chanceOfCard1)}% chance of being picked")
}