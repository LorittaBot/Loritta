package com.mrpowergamerbr.loritta.utils

fun chance(e: Double): Boolean {
	val d = Math.random()
	return d < e / 100.0
}