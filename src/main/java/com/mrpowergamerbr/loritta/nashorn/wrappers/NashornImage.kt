package com.mrpowergamerbr.loritta.nashorn.wrappers

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.nashorn.LorittaNashornException
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.utils.ImageUtils
import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage

/**
 * Wrapper para o BufferedImage, usado para imagens de comandos Nashorn
 */
class NashornImage {
	private var bufferedImage: BufferedImage
	private var graphics: Graphics

	constructor(x: Int, y: Int) {
		if (x > 1024 || y > 1024) {
			throw LorittaNashornException("Imagem grande demais!")
		}
		bufferedImage = BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB)
		graphics = bufferedImage.graphics
	}

	constructor(image: BufferedImage) {
		this.bufferedImage = image
		graphics = bufferedImage.graphics
	}

	@NashornCommand.NashornDocs(arguments = "texto, x, y")
	fun write(texto: String, x: Int, y: Int): NashornImage {
		if (texto.contains(Loritta.config.clientToken, true)) {
			NashornContext.securityViolation(null)
			return null!!
		}

		return write(texto, Color(0, 0, 0), x, y)
	}

	@NashornCommand.NashornDocs(arguments = "texto, cor, x, y")
	fun write(texto: String, cor: Color, x: Int, y: Int): NashornImage {
		if (texto.contains(Loritta.config.clientToken, true)) {
			NashornContext.securityViolation(null)
			return null!!
		}

		graphics!!.color = cor
		ImageUtils.drawTextWrap(texto, x, y, 9999, 9999, graphics.fontMetrics, graphics)
		return this
	}

	@NashornCommand.NashornDocs(arguments = "x, y")
	fun resize(x: Int, y: Int): NashornImage {
		if (x > 1024 || y > 1024) {
			throw LorittaNashornException("Imagem grande demais!")
		}
		return NashornImage(ImageUtils.toBufferedImage(bufferedImage.getScaledInstance(x, y, BufferedImage.SCALE_SMOOTH)))
	}

	@NashornCommand.NashornDocs(arguments = "image, x, y")
	fun paste(imagem: NashornImage, x: Int, y: Int): NashornImage {
		graphics.drawImage(imagem.bufferedImage, x, y, null)
		return this
	}

	@NashornCommand.NashornDocs(arguments = "x, y, w, h")
	fun paste(x: Int, y: Int, h: Int, w: Int): NashornImage {
		bufferedImage = bufferedImage.getSubimage(x, y, h, w)
		this.graphics = bufferedImage.graphics
		return this
	}

	@NashornCommand.NashornDocs(arguments = "cor, x, y, h, w")
	fun fillRectangle(color: Color, x: Int, y: Int, h: Int, w: Int): NashornImage {
		graphics.color = color
		graphics.fillRect(x, y, h, w)
		return this
	}
}