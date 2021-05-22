package net.perfectdreams.loritta.api.utils.extensions

import java.awt.Graphics2D
import java.awt.RenderingHints

fun Graphics2D.enableFontAntiAliasing(): Graphics2D {
	this.setRenderingHint(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
	return this
}