package net.perfectdreams.loritta.morenitta.utils

fun chance(e: Double): Boolean {
    val d = Math.random()
    return d < e / 100.0
}