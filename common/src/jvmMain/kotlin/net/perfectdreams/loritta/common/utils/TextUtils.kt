package net.perfectdreams.loritta.common.utils

import java.text.MessageFormat

actual fun String.format(vararg arguments: Any?): String {
	return MessageFormat.format(this, *arguments)
}