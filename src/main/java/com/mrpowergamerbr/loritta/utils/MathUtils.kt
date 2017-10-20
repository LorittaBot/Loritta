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

fun main(args: Array<String>) {
	println(MathUtils.convertToExtended("5k"))
	println(MathUtils.convertToExtended("2.5k"))
	println(MathUtils.convertToExtended("1kk500k"))
}

object MathUtils {
	fun convertToExtended(input: String): Double? {
		var switch = false
		var edgeCase = false
		for (ch in input.reversed().toLowerCase()) {
			if (switch && ch == 'k') {
				edgeCase = true
				break
			}
			if (ch != 'k') {
				switch = true
			}
		}
		if (edgeCase) { // 1kk500k
			val matcher = "([0-9.,]+) ?(k+)".toPattern().matcher(input.toLowerCase())

			var quantity = 0.0

			while (matcher.find()) {
				val value = matcher.group(1).toDouble()
				val powLevel = matcher.group(2).count { it == 'k' }.toDouble()
				quantity += Math.pow(1000.toDouble(), powLevel) * value
			}

			return quantity
		} else { // 2.5k
			var multiplier = Math.pow(1000.toDouble(), input.count { it == 'k' || it == 'K' }.toDouble())

			var numbers = input.toLowerCase().replace("k", "")

			val asDouble = numbers.toDoubleOrNull() ?: return null

			return asDouble * multiplier
		}
	}
}