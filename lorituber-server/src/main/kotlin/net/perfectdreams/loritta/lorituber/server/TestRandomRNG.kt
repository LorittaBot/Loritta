package net.perfectdreams.loritta.lorituber.server

import java.security.SecureRandom
import kotlin.time.measureTime

fun main() {
    val defaultSRandom = SecureRandom()
    println(defaultSRandom.algorithm)
    val sha1Random = SecureRandom.getInstance("Windows-PRNG")

    repeat(100) {
        measureTime {
            // defaultSRandom.ints(1_000_000, 0, 1000).toList()
            repeat(1_000_000) {
                defaultSRandom.nextInt()
            }
        }.also { println("Default SecureRandom: $it") }

        measureTime {
            // sha1Random.ints(1_000_000, 0, 1000).toList()
            repeat(1_000_000) {
                sha1Random.nextInt()
            }
        }.also { println("Default SHA1Random: $it") }
    }
}