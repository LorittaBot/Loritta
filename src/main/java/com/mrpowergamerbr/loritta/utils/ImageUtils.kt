package com.mrpowergamerbr.loritta.utils

import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Paint
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage

fun Graphics.drawText(text: String, x: Int, y: Int, maxX: Int? = null) {
	var currentX = x // X atual
	var currentY = y // Y atual
	var textToBeDrawn = text

	if (maxX != null) {
		var _text = ""
		var _x = x

		for (c in textToBeDrawn.toCharArray()) {
			val width = fontMetrics.charWidth(c) // Width do char (normalmente é 16)

			if (_x + width > maxX) {
				_text = _text.substring(0, _text.length - 4)
				_text += "..."
				break
			}
			_text += c
			_x += width
		}

		textToBeDrawn = _text
	}

	var idx = 0
	for (c in textToBeDrawn.toCharArray()) {
		idx++
		val width = fontMetrics.charWidth(c) // Width do char (normalmente é 16)
		if (!this.font.canDisplay(c)) {
			try {
				// Talvez seja um emoji!
				val imageUrl = "https://twemoji.maxcdn.com/2/72x72/" + LorittaUtils.toUnicode(textToBeDrawn.codePointAt(idx - 1)).substring(2) + ".png"
				val emoteImage = LorittaUtils.downloadImage(imageUrl)
				this.drawImage(emoteImage.getScaledInstance(this.font.size, this.font.size, BufferedImage.SCALE_SMOOTH), currentX, currentY - this.font.size + 1, null)
				currentX += fontMetrics.maxAdvance
			} catch (e: Exception) {
			}
			continue
		}
		this.drawString(c.toString(), currentX, currentY) // Escreva o char na imagem
		currentX += width // E adicione o width no nosso currentX
	}
}

object ImageUtils {
	/**
	 * Escreve um texto em um Graphics, fazendo wrap caso necessário
	 * @param text Texto
	 * @param startX X inicial
	 * @param startY Y inicial
	 * @param endX X máximo, caso o texto ultrapasse o endX, ele automaticamente irá fazer wrap para a próxima linha
	 * @param endY Y máximo, atualmente unused
	 * @param fontMetrics Metrics da fonte
	 * @param graphics Graphics usado para escrever a imagem
	 * @return Y final
	 */
	fun drawTextWrap(text: String, startX: Int, startY: Int, endX: Int, endY: Int, fontMetrics: FontMetrics, graphics: Graphics): Int {
		val lineHeight = fontMetrics.height // Aqui é a altura da nossa fonte

		var currentX = startX // X atual
		var currentY = startY // Y atual

		var idx = 0
		for (c in text.toCharArray()) {
			idx++
			val width = fontMetrics.charWidth(c) // Width do char (normalmente é 16)
			if (currentX + width > endX) { // Se o currentX é maior que o endX... (Nós usamos currentX + width para verificar "ahead of time")
				currentX = startX // Nós iremos fazer wrapping do texto
				currentY += lineHeight
			}
			if (!graphics.font.canDisplay(c)) {
				try {
					// Talvez seja um emoji!
					val imageUrl = "https://twemoji.maxcdn.com/2/72x72/" + LorittaUtils.toUnicode(text.codePointAt(idx - 1)).substring(2) + ".png"
					val emoteImage = LorittaUtils.downloadImage(imageUrl)
					graphics.drawImage(emoteImage.getScaledInstance(graphics.font.size, graphics.font.size, BufferedImage.SCALE_SMOOTH), currentX, currentY - graphics.font.size + 1, null)
					currentX += fontMetrics.maxAdvance
				} catch (e: Exception) {
				}

				continue
			}
			graphics.drawString(c.toString(), currentX, currentY) // Escreva o char na imagem
			currentX += width // E adicione o width no nosso currentX
		}
		return currentY
	}

	fun drawTextWrapUndertale(text: String, startX: Int, startY: Int, endX: Int, endY: Int, fontMetrics: FontMetrics, graphics: Graphics): Int {
		val temp = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)

		val lineHeight = fontMetrics.height // Aqui é a altura da nossa fonte
		val font = graphics.font // Font original
		var currentX = startX // X atual
		var currentY = startY // Y atual

		var idx = 0
		for (c in text.toCharArray()) {
			idx++
			val width = fontMetrics.charWidth(c) // Width do char (normalmente é 16)
			if (currentX + width > endX) { // Se o currentX é maior que o endX... (Nós usamos currentX + width para verificar "ahead of time")
				currentX = startX // Nós iremos fazer wrapping do texto
				currentY += lineHeight
			}
			if (font.canDisplay(c)) {
				graphics.drawString(c.toString(), currentX, currentY) // Escreva o char na imagem
			} else {
				try {
					// Talvez seja um emoji!
					val emoteImage = LorittaUtils.downloadImage("https://twemoji.maxcdn.com/2/72x72/" + LorittaUtils.toUnicode(text.codePointAt(idx - 1)).substring(2) + ".png")
					graphics.drawImage(emoteImage.getScaledInstance(width, width, BufferedImage.SCALE_SMOOTH), currentX, currentY - width, null)
					currentX += width
					continue
				} catch (e: Exception) {
				}

				if (temp.graphics.font.canDisplay(c)) {
					graphics.font = temp.graphics.font
					graphics.drawString(c.toString(), currentX, currentY) // Escreva o char na imagem

					graphics.font = font
				} else {
					continue
				}
			}
			currentX += width // E adicione o width no nosso currentX
		}
		return currentY
	}

	fun makeRoundedCorner(image: BufferedImage, cornerRadius: Int): BufferedImage {
		val w = image.width
		val h = image.height
		val output = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)

		val g2 = output.createGraphics()

		// This is what we want, but it only does hard-clipping, i.e. aliasing
		// g2.setClip(new RoundRectangle2D ...)

		// so instead fake soft-clipping by first drawing the desired clip shape
		// in fully opaque white with antialiasing enabled...
		g2.composite = AlphaComposite.Src
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
		g2.color = Color.WHITE
		g2.fill(RoundRectangle2D.Float(0f, 0f, w.toFloat(), h.toFloat(), cornerRadius.toFloat(), cornerRadius.toFloat()))

		// ... then compositing the image on top,
		// using the white shape from above as alpha source
		g2.composite = AlphaComposite.SrcAtop
		g2.drawImage(image, 0, 0, null)

		g2.dispose()

		return output
	}

	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	fun toBufferedImage(img: Image): BufferedImage {
		if (img is BufferedImage) {
			return img
		}

		// Create a buffered image with transparency
		val bimage = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)

		// Draw the image on to the buffered image
		val bGr = bimage.createGraphics()
		bGr.drawImage(img, 0, 0, null)
		bGr.dispose()

		// Return the buffered image
		return bimage
	}

	/**
	 * Draw a String centered in the middle of a Rectangle.
	 *
	 * @param graphics The Graphics instance.
	 * @param text The String to draw.
	 * @param rect The Rectangle to center the text in.
	 */
	fun drawCenteredString(graphics: Graphics, text: String, rect: Rectangle, font: Font) {
		// Get the FontMetrics
		val metrics = graphics.getFontMetrics(font)
		// Determine the X coordinate for the text
		val x = rect.x + (rect.width - metrics.stringWidth(text)) / 2
		// Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
		val y = rect.y + (rect.height - metrics.height) / 2 + metrics.ascent
		// Draw the String
		graphics.drawString(text, x, y)
	}

	/**
	 * Draw a String centered in the middle of a Rectangle.
	 *
	 * @param graphics The Graphics instance.
	 * @param text The String to draw.
	 * @param rect The Rectangle to center the text in.
	 */
	fun drawCenteredStringEmoji(graphics: Graphics, text: String, rect: Rectangle, font: Font) {
		graphics.font
		// Get the FontMetrics
		val metrics = graphics.getFontMetrics(font)
		// Determine the X coordinate for the text
		var x = rect.x + (rect.width - metrics.stringWidth(text)) / 2
		// Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
		val y = rect.y + (rect.height - metrics.height) / 2 + metrics.ascent

		var idx = 0
		for (c in text.toCharArray()) {
			idx++
			val width = graphics.fontMetrics.charWidth(c) // Width do char (normalmente é 16)
			if (!graphics.font.canDisplay(c)) {
				try {
					// Talvez seja um emoji!
					val imageUrl = "https://twemoji.maxcdn.com/2/72x72/" + LorittaUtils.toUnicode(text.codePointAt(idx - 1)).substring(2) + ".png"
					val emoteImage = LorittaUtils.downloadImage(imageUrl)
					graphics.drawImage(emoteImage.getScaledInstance(graphics.font.size, graphics.font.size, BufferedImage.SCALE_SMOOTH), x, y - graphics.font.size + 1, null)
					x += graphics.fontMetrics.maxAdvance
				} catch (e: Exception) {
				}

				continue
			}
			graphics.drawString(c.toString(), x, y) // Escreva o char na imagem
			x += width // E adicione o width no nosso currentX
		}
		// Draw the String
		graphics.drawString(text, x, y)
	}

	/**
	 * Draw a String centered in the middle of a Rectangle.
	 *
	 * @param graphics The Graphics instance.
	 * @param text The String to draw.
	 * @param rect The Rectangle to center the text in.
	 */
	fun drawCenteredStringOutlined(graphics: Graphics, text: String, rect: Rectangle, font: Font) {
		val color = graphics.color
		var g2d: Graphics2D? = null
		var paint: Paint? = null
		if (graphics is Graphics2D) {
			g2d = graphics
			paint = g2d.paint
		}
		// Get the FontMetrics
		val metrics = graphics.getFontMetrics(font)
		// Determine the X coordinate for the text
		val x = rect.x + (rect.width - metrics.stringWidth(text)) / 2
		// Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
		val y = rect.y + (rect.height - metrics.height) / 2 + metrics.ascent
		// Draw the outline
		graphics.color = Color.BLACK
		graphics.drawString(text, x - 1, y)
		graphics.drawString(text, x + 1, y)
		graphics.drawString(text, x, y - 1)
		graphics.drawString(text, x, y + 1)
		// Draw the String
		graphics.color = color
		if (paint != null) {
			g2d!!.paint = paint
		}
		graphics.drawString(text, x, y)
	}

	/**
	 * Escreve um texto em um Graphics, fazendo wrap caso necessário
	 *
	 * Esta versão separa entre espaços o texto, para ficar mais bonito
	 *
	 * @param text Texto
	 * @param startX X inicial
	 * @param startY Y inicial
	 * @param endX X máximo, caso o texto ultrapasse o endX, ele automaticamente irá fazer wrap para a próxima linha
	 * @param endY Y máximo, atualmente unused
	 * @param fontMetrics Metrics da fonte
	 * @param graphics Graphics usado para escrever a imagem
	 * @return Y final
	 */
	fun drawTextWrapSpaces(text: String, startX: Int, startY: Int, endX: Int, endY: Int, fontMetrics: FontMetrics, graphics: Graphics): Int {
		val lineHeight = fontMetrics.height // Aqui é a altura da nossa fonte

		var currentX = startX // X atual
		var currentY = startY // Y atual

		val split = text.split("((?<= )|(?= ))".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() // Nós precisamos deixar os espaços entre os splits!
		for (str in split) {
			var width = fontMetrics.stringWidth(str) // Width do texto que nós queremos colocar
			if (currentX + width > endX) { // Se o currentX é maior que o endX... (Nós usamos currentX + width para verificar "ahead of time")
				currentX = startX // Nós iremos fazer wrapping do texto
				currentY += lineHeight
			}
			var idx = 0
			for (c in str.toCharArray()) { // E agora nós iremos printar todos os chars
				idx++
				if (c == '\n') {
					currentX = startX // Nós iremos fazer wrapping do texto
					currentY += lineHeight
					continue
				}
				width = fontMetrics.charWidth(c)
				if (!graphics.font.canDisplay(c)) {
					try {
						// Talvez seja um emoji!
						val imageUrl = "https://twemoji.maxcdn.com/2/72x72/" + LorittaUtils.toUnicode(str.codePointAt(idx - 1)).substring(2) + ".png"
						val emoteImage = LorittaUtils.downloadImage(imageUrl)
						graphics.drawImage(emoteImage.getScaledInstance(width, width, BufferedImage.SCALE_SMOOTH), currentX, currentY - width, null)
						currentX += width
					} catch (e: Exception) {
					}

					continue
				}
				graphics.drawString(c.toString(), currentX, currentY) // Escreva o char na imagem
				currentX += width // E adicione o width no nosso currentX
			}
		}
		return currentY
	}
}