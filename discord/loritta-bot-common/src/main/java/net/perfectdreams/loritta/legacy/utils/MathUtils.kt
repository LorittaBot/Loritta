package net.perfectdreams.loritta.legacy.utils

fun chance(e: Double): Boolean {
	val d = Math.random()
	return d < e / 100.0
}