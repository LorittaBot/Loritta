package net.perfectdreams.loritta.cinnamon.microservices.dailytax.utils

import java.util.*

fun main() {
    val rand = SplittableRandom(0)
    val random = rand.nextInt(1, 101)

    var sum = 0

    repeat(30) {
        var multiplier = when (random) {
            100 -> { // 1
                6.0
            }
            in 94..99 -> { // 3
                5.0
            }
            in 78..93 -> { // 6
                4.0
            }
            in 59..77 -> { // 20
                3.0
            }
            in 34..58 -> { // 25
                2.0
            }
            in 0..33 -> { // 25
                1.5
            }
            else -> 1.1
        }

        var dailyPayout = rand.nextInt(
            1800 /* Math.max(555, 555 * (multiplier - 1)) */,
            ((1800 * multiplier) + 1).toInt()
        ) // 555 (lower bound) -> 555 * sites de votação do PerfectDreams

        println(dailyPayout)
        sum += dailyPayout
    }

    println("Sum: $sum")
}