package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.common.loricoolcards.CardRarity

fun main() {
    val totalBoosterPacks = mutableListOf<Int>()

    val cards = mutableListOf<Pair<Int, CardRarity>>()

    repeat(10) {
        cards.add(Pair(it + 1, CardRarity.MYTHIC))
    }

    repeat(500) { moneyIndex ->
        val rarity = if (moneyIndex in 0 until 10) {
            // 10 stickers = legendary
            CardRarity.LEGENDARY
        } else if (moneyIndex in 0 until 50) {
            // 40 stickers = epic
            CardRarity.EPIC
        } else if (moneyIndex in 0 until 150) {
            // 100 stickers = rare
            CardRarity.RARE
        } else if (moneyIndex in 0 until 300) {
            // 150 stickers = uncommon
            CardRarity.UNCOMMON
            // 200 stickers (the rest) = common
        } else {
            CardRarity.COMMON
        }

        cards.add(
            Pair(
                moneyIndex + 11,
                rarity
            )
        )
    }

    repeat(1_000) {
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