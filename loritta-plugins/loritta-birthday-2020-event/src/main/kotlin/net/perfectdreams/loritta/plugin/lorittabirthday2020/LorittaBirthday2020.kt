package net.perfectdreams.loritta.plugin.lorittabirthday2020

object LorittaBirthday2020 {
	val emojis = listOf(
			"lori_gift:653402818199158805",
			"green_gift:659069659160772647",
			"pink_gift:659069658833354773"
	)

	fun isEventActive(): Boolean {
		// val calendar = Calendar.getInstance()
		// return calendar.get(Calendar.YEAR) == 2020
		return true
	}
}