package com.mrpowergamerbr.loritta.utils

fun getPercentage(val1: Double, val2: Double): Double {
	return val1 / val2 * 100
}

fun chance(e: Double): Boolean {
	val d = Math.random()
	return d < e / 100.0
}

infix fun Double.percentage(val2: Double): Double {
	return getPercentage(this, val2)
}