package net.perfectdreams.loritta.plugin.christmas2019

import java.util.*

object Christmas2019 {
	val emojis = listOf(
			"lori_gift:653402818199158805",
			"green_gift:659069659160772647",
			"pink_gift:659069658833354773",
			"blue_heart:659069659089338388",
			"pink_heart:659069658787348496",
			"purple_heart:659069659181613066",
			"green_heart:659069659009646615",
			"christmas_hat:659069660163080211",
			"\uD83C\uDF84"
	)

	fun isEventActive(): Boolean {
		val calendar = Calendar.getInstance()
		return calendar.get(Calendar.YEAR) == 2019
	}
}