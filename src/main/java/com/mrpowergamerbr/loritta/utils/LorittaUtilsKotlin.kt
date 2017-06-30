package com.mrpowergamerbr.loritta.utils

import java.text.DateFormatSymbols
import java.time.OffsetDateTime

inline fun OffsetDateTime.humanize(): String {
	val months = DateFormatSymbols().getMonths();
	return "${this.dayOfMonth} de ${months[this.month.value - 1]}, ${this.year} às ${this.hour.toString().padStart(2, '0')}:${this.minute.toString().padStart(2, '0')}";
}