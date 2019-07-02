package net.perfectdreams.spicymorenitta.utils.extensions

import org.w3c.dom.Element
import kotlin.browser.window

fun Element.offset(): Offset {
	val rect = this.getBoundingClientRect()
	val scrollLeft = window.pageXOffset
	val scrollTop = window.pageYOffset
	return Offset(
			rect.top + scrollTop,
			rect.left + scrollLeft
	)
}

fun Element.width() = this.getBoundingClientRect().width

data class Offset(
		val top: Double,
		val left: Double
)