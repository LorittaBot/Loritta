package com.mrpowergamerbr.loritta.utils.extensions

import com.mrpowergamerbr.loritta.utils.ImageUtils
import java.awt.Color
import java.awt.Graphics

/**
 * Draws a string with a outline around it, the text will be drawn with the current color set in the graphics object
 *
 * @param graphics     the image graphics
 * @param text         the text that will be drawn
 * @param x            where the text will be drawn in the x-axis
 * @param y            where the text will be drawn in the y-axis
 * @param outlineColor the color of the outline
 * @param power        the thickness of the outline
 *
 * @see [ImageUtils.drawStringWithOutline]
 */
fun Graphics.drawStringWithOutline(text: String, x: Int, y: Int, outlineColor: Color = Color.BLACK, power: Int = 2) =
		ImageUtils.drawStringWithOutline(this, text, x, y, outlineColor, power)