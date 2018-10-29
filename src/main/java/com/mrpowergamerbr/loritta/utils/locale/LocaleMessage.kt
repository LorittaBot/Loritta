package com.mrpowergamerbr.loritta.utils.locale

import java.text.MessageFormat

inline class LocaleMessage(val raw: String?) {
	override fun toString(): String {
		return MessageFormat.format(raw)
	}

	operator fun get(vararg arguments: Any?): String {
		return MessageFormat.format(raw, *arguments)
	}
}