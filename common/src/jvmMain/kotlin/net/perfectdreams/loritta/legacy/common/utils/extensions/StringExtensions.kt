package net.perfectdreams.loritta.legacy.common.utils.extensions

import java.text.MessageFormat

actual fun String.format(vararg arguments: Any?): String {
    return MessageFormat.format(this, *arguments)
}