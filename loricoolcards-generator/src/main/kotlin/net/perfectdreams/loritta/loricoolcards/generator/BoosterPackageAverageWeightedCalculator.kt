package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.common.loricoolcards.CardRarity
import kotlin.random.Random
import kotlin.time.measureTime

fun main() {
    // Define weights corresponding to the enum values
    val weights = mapOf(
        CardRarity.COMMON to 26.0,
        CardRarity.UNCOMMON to 23.0,
        CardRarity.RARE to 20.0,
        CardRarity.EPIC to 13.0,
        CardRarity.LEGENDARY to 10.0,
        CardRarity.MYTHIC to 8.0,
    )

    println("Total weight value: ${weights.values.sum()}")

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

    val totalBoosterPacks = mutableListOf<Int>()

    val weightedCards = cards.associate { it.first to weights[it.second]!! }

    measureTime {
        repeat(10_000) {
            // println(it)
            val totalCards = (1..510).toList()
            val currentCards = mutableSetOf<Int>()

            var boosterPacks = 0

            while (!currentCards.containsAll(totalCards)) {
                val randomRarities = weightedRandomSelection(weightedCards, 5)

                for (cardId in randomRarities) {
                    currentCards.add(cards.first { it.first == cardId }.first)
                }

                boosterPacks++
            }


            totalBoosterPacks.add(boosterPacks)
        }

        println("Total Booster Packs: ${totalBoosterPacks.average()}")
    }.also { println("Took $it") }
}

private fun weightedRandomSelection(weights: Map<Int, Double>, n: Int): List<Int> {
    val weightedValues = ArrayList<Pair<Int, Double>>(weights.size)
    var totalWeight = 0.0

    for ((rarity, weight) in weights) {
        totalWeight += weight
        weightedValues.add(rarity to totalWeight)
    }

    return List(n) {
        val randomValue = Random.nextDouble(totalWeight)
        binarySearch(weightedValues, randomValue)
    }
}

private fun binarySearch(weightedValues: List<Pair<Int, Double>>, randomValue: Double): Int {
    var low = 0
    var high = weightedValues.size - 1

    while (low < high) {
        val mid = (low + high) / 2
        if (weightedValues[mid].second < randomValue)
            low = mid + 1
        else
            high = mid
    }
    return weightedValues[low].first
}