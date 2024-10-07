package net.perfectdreams.loritta.lorituber

fun main() {
    println(easeOutExpo(0.5))
}

private fun easeOutExpo(x: Double): Double {
    return if (x == 1.0) 1.0 else 1 - Math.pow(2.0, -10 * x)
}