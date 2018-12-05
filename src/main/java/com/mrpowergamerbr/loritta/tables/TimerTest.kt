package com.mrpowergamerbr.loritta.tables

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.thread

fun main(args: Array<String>) {
	val timer = Timer(3600000 * 0, null, 15000)
	timer.calculateRepetitions()
	GlobalScope.launch { timer.doStuff() }

	thread { while (true) { }}
}

class Timer(
		val startsAt: Long, // relativo ao começo do dia, milliseconds
		val repeatCount: Int?, // quantas vezes o timer repete, caso seja null = infinitas vezes
		val repeatDelay: Long // delay entre cada repetição
) {
	fun calculateRepetitions() {
		val repeat = repeatCount ?: 10

		var first = startsAt

		repeat(repeat) {
			print("$it às $first")
			val absoluteTime = first + getStartOfDay()
			if (System.currentTimeMillis() > absoluteTime) {
				print(" (Passado! ${System.currentTimeMillis()}/$absoluteTime)")
			} else {
				print(" (Será executado daqui ${absoluteTime - System.currentTimeMillis()}ms!)")
			}
			print("\n")
			first += repeatDelay
		}
	}

	suspend fun doStuff() {
		println("doStuff()")
		var simulatedTime = startsAt

		var i = 0
		val compare = if (repeatCount == null) {
			{ true }
		} else { { repeatCount > i } }

		while (compare.invoke()) {
			println("${System.currentTimeMillis()} / $simulatedTime")

			val relativeTimeNow = System.currentTimeMillis() - getStartOfDay()

			if (simulatedTime > relativeTimeNow) {
				println("$i - uwu!!! (Será executado daqui ${simulatedTime - relativeTimeNow}ms!)")

				val start = System.currentTimeMillis()
				delay(simulatedTime - relativeTimeNow)

				println(System.currentTimeMillis() - start)

				execute()
				doStuff()
				return
			} else {
				println("$i - Passado...")
			}
			simulatedTime += repeatDelay
			i++
		}
	}

	fun execute() {
		println("Triggered timer!")
	}

	fun getStartOfDay(): Long {
		val calendar = Calendar.getInstance()
		calendar.set(Calendar.HOUR_OF_DAY, 0)
		calendar.set(Calendar.MINUTE, 0)
		calendar.set(Calendar.SECOND, 0)
		calendar.set(Calendar.MILLISECOND, 0)

		return calendar.timeInMillis
	}
}