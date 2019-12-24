package net.perfectdreams.loritta.plugin.christmas2019

import java.util.*

object Christmas2019 {
	fun isEventActive(): Boolean {
		val calendar = Calendar.getInstance()
		return calendar.get(Calendar.YEAR) == 2019
	}
}