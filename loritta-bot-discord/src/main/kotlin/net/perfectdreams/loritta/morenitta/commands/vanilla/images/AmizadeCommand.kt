package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.enableFontAntiAliasing
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.extensions.getEffectiveAvatarUrl
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import java.awt.Color
import java.awt.GradientPaint
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class AmizadeCommand(loritta: LorittaBot) : AbstractCommand(loritta, "friendship", listOf("amizade"), net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES) {
	companion object {
		val TEMPLATE_OVERLAY by lazy { ImageIO.read(File(Constants.ASSETS_FOLDER, "amizade_overlay.png")) }
	}

	override fun getDescriptionKey() = LocaleKeyData("commands.command.friendship.description")
	// TODO: Needs to be two users
	// override fun getExamplesKey() = Command.TWO_IMAGES_EXAMPLES_KEY

	// TODO: Fix Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.message.mentions.users.size == 2) {
			// Não podemos usar context...
			val user = context.message.mentions.users[0]
			val user2 = context.message.mentions.users[1]

			val avatar = LorittaUtils.downloadImage(loritta, context.userHandle.getEffectiveAvatarUrl(ImageFormat.PNG, 128))
			val avatar2 = LorittaUtils.downloadImage(loritta, user.getEffectiveAvatarUrl(ImageFormat.PNG, 128))
			val avatar3 = LorittaUtils.downloadImage(loritta, user2.getEffectiveAvatarUrl(ImageFormat.PNG, 128))

			val template = readImage(File(LorittaBot.ASSETS + "amizade.png")) // Template

			val graphics = template.graphics.enableFontAntiAliasing() // É necessário usar Graphics2D para usar gradients

			// Colocar todos os avatares
			graphics.drawImage(avatar!!.getScaledInstance(108, 108, BufferedImage.SCALE_SMOOTH), 55, 10, null)
			graphics.drawImage(avatar3!!.getScaledInstance(110, 110, BufferedImage.SCALE_SMOOTH), 232, 54, null)
			graphics.drawImage(avatar2!!.getScaledInstance(85, 134, BufferedImage.SCALE_SMOOTH), 0, 166, null)
			graphics.drawImage(avatar2.getScaledInstance(111, 120, BufferedImage.SCALE_SMOOTH), 289, 180, null)

			// E colocar o overlay da imagem
			graphics.drawImage(TEMPLATE_OVERLAY, 0, 0, null)

			var font = graphics.font.deriveFont(21F)
			graphics.font = font
			var fontMetrics = graphics.getFontMetrics(font)

			val friendshipEnded = locale["commands.command.friendship.friendWith", user.name]
			var gp = GradientPaint(
					0.0f, 0.0f,
                    Color(202, 72, 15),
					0.0f, fontMetrics.height.toFloat() + fontMetrics.height.toFloat(),
					Color(66, 181, 33))
			graphics.paint = gp

			ImageUtils.drawCenteredStringOutlined(graphics, friendshipEnded, Rectangle(0, 10, 400, 30), font)
			graphics.color = Color.RED

			font = font.deriveFont(30F)
			graphics.font = font

			ImageUtils.drawCenteredStringOutlined(graphics, locale["commands.command.friendship.ended"], Rectangle(0, 30, 400, 40), font)

			font = font.deriveFont(24F)
			graphics.font = font
			fontMetrics = graphics.getFontMetrics(font)
			gp = GradientPaint(
					0.0f, 140f,
					Color(206, 7, 129),
					0.0f, 190f,
					Color(103, 216, 11))
			graphics.paint = gp
			// graphics.fillRect(0, 0, 400, 300); // debugging
			ImageUtils.drawCenteredStringOutlined(graphics, "${locale["commands.command.friendship.now"]} " + user2.name, Rectangle(0, 100, 400, 110), font)
			ImageUtils.drawCenteredStringOutlined(graphics, locale["commands.command.friendship.isMy"], Rectangle(0, 120, 400, 130), font)
			graphics.color = Color.MAGENTA
			ImageUtils.drawCenteredStringOutlined(graphics, locale["commands.command.friendship.bestFriend"], Rectangle(0, 140, 400, 150), font)

			context.sendFile(template, "rip_amizade.png", context.getAsMention(true))
		} else {
			context.explain()
		}
	}
}