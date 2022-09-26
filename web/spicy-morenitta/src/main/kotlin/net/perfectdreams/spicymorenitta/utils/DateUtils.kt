package net.perfectdreams.spicymorenitta.utils

object DateUtils {
	fun formatDateDiff(fromDate: Double, toDate: Double): String {
		val diff = toDate - fromDate

		val diffSeconds = (diff / 1000 % 60).toInt()
		val diffMinutes = (diff / (60 * 1000) % 60).toInt()
		val diffHours = (diff / (60 * 60 * 1000) % 24).toInt()
		val diffDays = (diff / (24 * 60 * 60 * 1000)).toInt()

		return "$diffDays dias, $diffHours horas, $diffMinutes minutos, $diffSeconds segundos"
	}
}