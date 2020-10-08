package net.perfectdreams.loritta.embededitor.utils

class TingleOptions(
		val footer: Boolean? = null,
		val stickyFooter: Boolean? = null,
		val closeMethods: Array<String> = arrayOf(),
		val closeLabel: String? = null,
		val cssClass: Array<String> = arrayOf(),
		val onOpen: (() -> Unit)? = null,
		val onClose: (() -> Unit)? = null,
		val beforeClose: (() -> Boolean)? = null
)