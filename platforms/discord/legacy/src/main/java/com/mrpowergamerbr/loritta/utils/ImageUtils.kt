package com.mrpowergamerbr.loritta.utils

import com.google.common.cache.CacheBuilder
import com.mrpowergamerbr.loritta.utils.extensions.getOrNull
import java.awt.*
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.streams.toList

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
			// Talvez seja um emoji!
			val emoteImage = ImageUtils.getTwitterEmoji(textToBeDrawn, idx)
			if (emoteImage != null) {
				this.drawImage(emoteImage.getScaledInstance(this.font.size, this.font.size, BufferedImage.SCALE_SMOOTH), currentX, currentY - this.font.size + 1, null)
				currentX += fontMetrics.maxAdvance
			}

			continue
		}
		this.drawString(c.toString(), currentX, currentY) // Escreva o char na imagem
		currentX += width // E adicione o width no nosso currentX
	}
}

fun Graphics.enableFontAntiAliasing(): Graphics2D {
	this as Graphics2D
	this.setRenderingHint(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
	return this
}

object ImageUtils {
	val emotes = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build<String, Optional<BufferedImage>>().asMap()

	fun getTwitterEmojiUrlId(emoji: String) = emoji.codePoints().toList().joinToString(separator = "-") { LorittaUtils.toUnicode(it).substring(2) }

	fun getTwitterEmoji(text: String, index: Int): BufferedImage? {
		try {
			val imageUrl = "https://twemoji.maxcdn.com/2/72x72/" + LorittaUtils.toUnicode(text.codePointAt(index - 1)).substring(2) + ".png"
			try {
				if (emotes.containsKey(imageUrl))
					return emotes[imageUrl]?.getOrNull()

				val emoteImage = LorittaUtils.downloadImage(imageUrl)
				emotes[imageUrl] = Optional.ofNullable(emoteImage)
				return emoteImage
			} catch (e: Exception) {
				// Outro try ... catch, esse é usado para evitar baixar imagens inexistentes, mas que o codepoint existe
				emotes[imageUrl] = Optional.empty()
				return null
			}
		} catch (e: Exception) {
			return null
		}
	}

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
				val emoteImage = getTwitterEmoji(text, idx)
				if (emoteImage != null) {
					graphics.drawImage(emoteImage.getScaledInstance(graphics.font.size, graphics.font.size, BufferedImage.SCALE_SMOOTH), currentX, currentY - graphics.font.size + 1, null)
					currentX += fontMetrics.maxAdvance
				}
				continue
			}
			graphics.drawString(c.toString(), currentX, currentY) // Escreva o char na imagem
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
				// Talvez seja um emoji!
				val emoteImage = getTwitterEmoji(text, idx)
				if (emoteImage != null) {
					graphics.drawImage(emoteImage.getScaledInstance(graphics.font.size, graphics.font.size, BufferedImage.SCALE_SMOOTH), x, y - graphics.font.size + 1, null)
					x += graphics.fontMetrics.maxAdvance
				}

				continue
			}
			graphics.drawString(c.toString(), x, y) // Escreva o char na imagem
			x += width // E adicione o width no nosso currentX
		}
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
					// Talvez seja um emoji!
					val emoteImage = getTwitterEmoji(str, idx)
					if (emoteImage != null) {
						graphics.drawImage(emoteImage.getScaledInstance(width, width, BufferedImage.SCALE_SMOOTH), currentX, currentY - width, null)
						currentX += width
					}

					continue
				}
				graphics.drawString(c.toString(), currentX, currentY) // Escreva o char na imagem
				currentX += width // E adicione o width no nosso currentX
			}
		}
		return currentY
	}

	/**
	 * Creates an image containing the [text] centralized on it
	 */
	fun createTextAsImage(width: Int, height: Int, text: String): BufferedImage {
		val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
		val graphics = image.graphics
		graphics.color = Color.WHITE
		graphics.fillRect(0, 0, width, height)
		val font = image.graphics.font.deriveFont(Font.BOLD, 22f)
		graphics.font = font
		graphics.color = Color.BLACK
		val fontMetrics = graphics.fontMetrics

		// Para escrever uma imagem centralizada, nós precisamos primeiro saber algumas coisas sobre o texto

		// Lista contendo (texto, posição)
		val lines = mutableListOf<String>()

		// Se está centralizado verticalmente ou não, por enquanto não importa para a gente
		val split = text.split(" ")

		var x = 0
		var currentLine = StringBuilder()

		for (string in split) {
			val stringWidth = fontMetrics.stringWidth("$string ")
			val newX = x + stringWidth

			if (newX >= width) {
				var endResult = currentLine.toString().trim()
				if (endResult.isEmpty()) { // okay wtf
					// Se o texto é grande demais e o conteúdo atual está vazio... bem... substitua o endResult pela string atual
					endResult = string
					lines.add(endResult)
					x = 0
					continue
				}
				lines.add(endResult)
				currentLine = StringBuilder()
				currentLine.append(' ')
				currentLine.append(string)
				x = fontMetrics.stringWidth("$string ")
			} else {
				currentLine.append(' ')
				currentLine.append(string)
				x = newX
			}
		}
		lines.add(currentLine.toString().trim())

		// got it!!!
		// bem, supondo que cada fonte tem 22f de altura...

		// para centralizar é mais complicado
		val skipHeight = fontMetrics.ascent
		var y = (height / 2) - ((skipHeight - 10) * (lines.size - 1))
		for (line in lines) {
			ImageUtils.drawCenteredStringEmoji(graphics, line, Rectangle(0, y, width, 24), font)
			y += skipHeight
		}

		return image
	}

	/**
	 * Draws a string with a outline around it, the text will be drawn with the current color set in the graphics object
	 *
	 * @param graphics     the image graphics
	 * @param text         the text that will be drawn
	 * @param x            where the text will be drawn in the x-axis
	 * @param y            where the text will be drawn in the y-axis
	 * @param outlineColor the color of the outline
	 * @param power        the thickness of the outline
	 */
	fun drawStringWithOutline(graphics: Graphics, text: String, x: Int, y: Int, outlineColor: Color = Color.BLACK, power: Int = 2) {
		val originalColor = graphics.color
		graphics.color = outlineColor
		for (powerX in -power..power) {
			for (powerY in -power..power) {
				graphics.drawString(text, x + powerX, y + powerY)
			}
		}

		graphics.color = originalColor
		graphics.drawString(text, x, y)
	}
}