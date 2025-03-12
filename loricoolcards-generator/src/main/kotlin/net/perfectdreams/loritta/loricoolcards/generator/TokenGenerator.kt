package net.perfectdreams.loritta.loricoolcards.generator

import net.perfectdreams.loritta.morenitta.utils.Base58
import java.security.SecureRandom

fun main() {
    val secureRandom = SecureRandom()

    val randomBytes = ByteArray(32).apply {
        secureRandom.nextBytes(this)
    }
    val token = "lorixb_${Base58.encode(randomBytes)}"

    println(token)
}