package net.perfectdreams.loritta.loricoolcards.generator

fun main() {
    val totalBoosterPacks = mutableListOf<Int>()

    repeat(100_000) {
        val totalCards = (1..510).toList()
        val currentCards = mutableSetOf<Int>()

        var boosterPacks = 0

        while (!currentCards.containsAll(totalCards)) {
            currentCards.add(totalCards.random())
            currentCards.add(totalCards.random())
            currentCards.add(totalCards.random())
            currentCards.add(totalCards.random())
            currentCards.add(totalCards.random())
            boosterPacks++
        }


        totalBoosterPacks.add(boosterPacks)
    }

    println("Total Booster Packs: ${totalBoosterPacks.average()}")
}