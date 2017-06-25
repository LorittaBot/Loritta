package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import java.awt.Color
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.awt.image.FilteredImageSource
import java.awt.image.RGBImageFilter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

class RazoesCommand : CommandBase() {
	override fun getLabel(): String {
		return "razoes"
	}

	override fun getAliases(): List<String> {
		return listOf("razões", "reasons")
	}

	override fun getDescription(): String {
		return "Qual é a sua razão para viver?";
	}

	override fun getExample(): List<String> {
		return listOf("@Loritta");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN;
	}

	override fun run(context: CommandContext) {
		var contextImage = LorittaUtils.getImageFromContext(context, 0);
		if (!LorittaUtils.isValidImage(context, contextImage)) {
			return;
		}
		var template = ImageIO.read(File(Loritta.FOLDER + "reasons.png")); // Template
		var image = BufferedImage(346, 600, BufferedImage.TYPE_INT_ARGB)

		var graphics = image.graphics;
		var skewed = javaxt.io.Image(contextImage);

		skewed.resize(202, 202);

		// Vamos baixar o avatar do usuário
		var avatar = LorittaUtils.downloadImage(context.userHandle.effectiveAvatarUrl)

		// Agora nós iremos pegar a cor mais prevalente na imagem do avatar do usuário
		var dominantImage = ImageUtils.toBufferedImage(avatar.getScaledInstance(1, 1, BufferedImage.SCALE_AREA_AVERAGING));
		var dominantColor = dominantImage.getRGB(0, 0);

		var red = (dominantColor shr 16) and 0xFF;
		var green = (dominantColor shr 8) and 0xFF;
		var blue = dominantColor and 0xFF;

		// Aplicar nosso filtro
		var colorFilter = MagentaDominantSwapFilter(red, green, blue)

		var newTemplate = FilteredImageSource(template.source, colorFilter);
		template = ImageUtils.toBufferedImage(Toolkit.getDefaultToolkit().createImage(newTemplate));

		skewed.width = 240; // Aumentar o tamanho da imagem para manipular ela
		skewed.height = 240;
		// skew image
		skewed.setCorners(
				// keep the upper left corner as it is
				0F,0F, // UL

				// push the upper right corner more to the bottom
				202 - 40F,40F , // UR

				// push the lower right corner more to the left
				236F,210F, // LR

				// push the lower left corner more to the right
				95F, 215F); // LL

		graphics.drawImage(skewed.bufferedImage, 30, 370, null);

		graphics.drawImage(template, 0, 0, null); // Desenhe o template por cima!

		// Agora nós vamos colar o avatar em cima do template
		// Vamos usar o javaxt porque é bem mais fácil
		var rotatedAvatar = javaxt.io.Image(avatar);
		rotatedAvatar.resize(109, 109)
		rotatedAvatar.rotate(5.0)
		graphics.drawImage(rotatedAvatar.bufferedImage, 188, 4, null)

		val os = ByteArrayOutputStream()
		ImageIO.write(image, "png", os)
		val inputStream = ByteArrayInputStream(os.toByteArray())

		context.sendFile(inputStream, "reasons.png", context.getAsMention(true));
	}
}

class MagentaDominantSwapFilter : RGBImageFilter {
	var newR: Int = 0;
	var newG: Int = 0;
	var newB: Int = 0;

	constructor(newR: Int, newG: Int, newB: Int) {
		canFilterIndexColorModel = false;
		this.newR = newR;
		this.newG = newG;
		this.newB = newB;
	}

	override fun filterRGB(x: Int, y: Int, rgb: Int): Int {
		var red = (rgb shr 16) and 0xFF;
		var green = (rgb shr 8) and 0xFF;
		var blue = rgb and 0xFF;

		if (red == 255 && green == 0 && blue == 255) {
			return Color(newR, newB, newG).rgb;
		}
		return rgb;
	}
}