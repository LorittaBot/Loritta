package net.perfectdreams.loritta.common.utils

import java.util.*

object WeightedRandom {
    fun <T> random(random: Random, items: List<Item<T>>): Item<T> {
        val totalWeight = items.sumOf { it.weight }
        val randomValue = random.nextInt(totalWeight)

        var cumulativeWeight = 0
        for (item in items) {
            cumulativeWeight += item.weight
            if (randomValue < cumulativeWeight) {
                return item
            }
        }

        // Fallback (should never happen if weights are positive)
        throw IllegalArgumentException("Weights must be positive and non-empty")
    }

    data class Item<T>(val value: T, val weight: Int)
}