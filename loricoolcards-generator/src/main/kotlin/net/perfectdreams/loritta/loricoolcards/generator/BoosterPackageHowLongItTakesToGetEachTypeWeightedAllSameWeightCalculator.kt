package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.common.loricoolcards.CardRarity
import kotlin.random.Random
import kotlin.time.measureTime

fun main() {
    // Define weights corresponding to the enum values
    val weights = mapOf(
        CardRarity.COMMON to 1.0
    )

    println("Total weight value: ${weights.values.sum()}")

    val cards = mutableListOf<Pair<Int, CardRarity>>()

    repeat(10) {
        cards.add(Pair(it + 1, CardRarity.COMMON))
    }

    repeat(500) { moneyIndex ->
        val rarity = CardRarity.COMMON

        cards.add(
            Pair(
                moneyIndex + 11,
                rarity
            )
        )
    }

    cards.groupBy { it.second }
        .forEach {
            println(it.key.toString() + ": " + it.value.size)
        }

    val totalBoosterPacks = mutableListOf<Int>()

    val weightedCards = cards.associate { it.first to weights[it.second]!! }

    measureTime {
        repeat(10_000) {
            println(it)
            val currentRaritiesCards = mutableListOf<CardRarity>()

            var boosterPacks = 0

            while (true) {
                var diff = false
                for (rarity in CardRarity.entries) {
                    val gameCount = cards.count { it.second == rarity }
                    if (gameCount > currentRaritiesCards.count { it == rarity }) {
                        diff = true
                        break
                    }
                }

                if (!diff)
                    break

                val randomRarities = weightedRandomSelection(weightedCards, 5)

                for (cardId in randomRarities) {
                    currentRaritiesCards.add(cards.first { it.first == cardId }.second)
                }

                boosterPacks++
            }


            totalBoosterPacks.add(boosterPacks)

            currentRaritiesCards.groupBy { it }
                .entries
                .sortedBy { it.key.ordinal }
                .forEach {
                    println(it.key.toString() + ": " + it.value.size)
                }
            println("Stickers: ${currentRaritiesCards.size}")
            println("Booster packs: ${boosterPacks}")
            println("Total Booster Packs (average): ${totalBoosterPacks.average()}")
            println("Total Booster Packs (minimum): ${totalBoosterPacks.min()}")
            println("Total Booster Packs (maxium): ${totalBoosterPacks.max()}")
        }

        // println("Total Booster Packs (average): ${totalBoosterPacks.average()}")
        // println("Total Booster Packs (minimum): ${totalBoosterPacks.min()}")
        // println("Total Booster Packs (maxium): ${totalBoosterPacks.max()}")
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
        val randomValue = fixedRandom.nextDouble(totalWeight)
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