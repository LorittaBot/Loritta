package com.mrpowergamerbr.loritta.commands.vanilla.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaImage
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.utils.ImageFormat
import net.perfectdreams.loritta.utils.extensions.getEffectiveAvatarUrl
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.Color
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.awt.image.FilteredImageSource
import java.awt.image.RGBImageFilter
import java.io.File

class RazoesCommand : AbstractCommand("reasons", listOf("razões", "razoes"), CommandCategory.IMAGES) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.reasons.description")
	override fun getExamplesKey() = Command.SINGLE_IMAGE_EXAMPLES_KEY


	override fun needsToUploadFiles() = true

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val contextImage = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		var template = readImage(File(Loritta.ASSETS + "reasons.png")) // Template
		val image = BufferedImage(346, 600, BufferedImage.TYPE_INT_ARGB)

		val graphics = image.graphics
		val skewed = LorittaImage(contextImage)

		skewed.resize(202, 202)

		// Vamos baixar o avatar do usuário
		val avatar = LorittaUtils.downloadImage(context.userHandle.getEffectiveAvatarUrl(ImageFormat.PNG, 128))

		// Agora nós iremos pegar a cor mais prevalente na imagem do avatar do usuário
		val dominantImage = ImageUtils.toBufferedImage(avatar!!.getScaledInstance(1, 1, BufferedImage.SCALE_AREA_AVERAGING))
		val dominantColor = dominantImage.getRGB(0, 0)

		val red = (dominantColor shr 16) and 0xFF
		val green = (dominantColor shr 8) and 0xFF
		val blue = dominantColor and 0xFF

		// Aplicar nosso filtro
		val colorFilter = MagentaDominantSwapFilter(red, green, blue)

		val newTemplate = FilteredImageSource(template.source, colorFilter)
		template = ImageUtils.toBufferedImage(Toolkit.getDefaultToolkit().createImage(newTemplate))

		skewed.width = 240 // Aumentar o tamanho da imagem para manipular ela
		skewed.height = 240
		// skew image
		skewed.setCorners(
				// keep the upper left corner as it is
				0F,0F, // UL

				// push the upper right corner more to the bottom
				202 - 40F,40F , // UR

				// push the lower right corner more to the left
				236F,210F, // LR

				// push the lower left corner more to the right
				95F, 215F) // LL

		graphics.drawImage(skewed.bufferedImage, 30, 370, null)

		graphics.drawImage(template, 0, 0, null) // Desenhe o template por cima!

		// Agora nós vamos colar o avatar em cima do template
		// Vamos usar o javaxt porque é bem mais fácil
		var rotatedAvatar = LorittaImage(avatar)
		rotatedAvatar.resize(109, 109)
		rotatedAvatar.rotate(5.0)
		graphics.drawImage(rotatedAvatar.bufferedImage, 188, 4, null)

		context.sendFile(image, "reasons.png", context.getAsMention(true))
	}
}

class MagentaDominantSwapFilter : RGBImageFilter {
	var newR: Int = 0
	var newG: Int = 0
	var newB: Int = 0

	constructor(newR: Int, newG: Int, newB: Int) {
		canFilterIndexColorModel = false
		this.newR = newR
		this.newG = newG
		this.newB = newB
	}

	override fun filterRGB(x: Int, y: Int, rgb: Int): Int {
		var red = (rgb shr 16) and 0xFF
		var green = (rgb shr 8) and 0xFF
		var blue = rgb and 0xFF

		if (red == 255 && green == 0 && blue == 255) {
			return Color(newR, newB, newG).rgb
		}
		return rgb
	}
}