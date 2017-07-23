package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.Color
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class AmizadeCommand : CommandBase() {
	override fun getLabel(): String {
		return "amizade"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.AMIZADE_DESCRIPTION.f()
	}

	override fun getExample(): List<String> {
		return listOf("@Tatsumaki @Loritta");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN;
	}

	override fun getUsage(): String {
		return "<usuário 1> <usuário 2>";
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext) {
		if (context.message.mentionedUsers.size == 2) {
			// Não podemos usar context...
			val user = context.message.mentionedUsers[0]
			val user2 = context.message.mentionedUsers[1]

			val avatar = LorittaUtils.downloadImage(context.userHandle.effectiveAvatarUrl)
			val avatar2 = LorittaUtils.downloadImage(user.effectiveAvatarUrl)
			val avatar3 = LorittaUtils.downloadImage(user2.effectiveAvatarUrl)

			val template = ImageIO.read(File(Loritta.FOLDER + "amizade.png")); // Template

			val graphics = template.graphics as Graphics2D // É necessário usar Graphics2D para usar gradients

			// Colocar todos os avatares
			graphics.drawImage(avatar.getScaledInstance(108, 108, BufferedImage.SCALE_SMOOTH), 55, 10, null)
			graphics.drawImage(avatar3.getScaledInstance(110, 110, BufferedImage.SCALE_SMOOTH), 232, 54, null)
			graphics.drawImage(avatar2.getScaledInstance(85, 134, BufferedImage.SCALE_SMOOTH), 0, 166, null)
			graphics.drawImage(avatar2.getScaledInstance(111, 120, BufferedImage.SCALE_SMOOTH), 289, 180, null)

			// E colocar o overlay da imagem
			val overlay = ImageIO.read(File(Loritta.FOLDER + "amizade_overlay.png")); // Template
			graphics.drawImage(overlay, 0, 0, null)

			var font = graphics.font.deriveFont(21F)
			graphics.font = font
			graphics.setRenderingHint(
					java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
					java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			var fontMetrics = graphics.getFontMetrics(font)

			val friendshipEnded = context.locale.AMIZADE_AMIZADE_COM.f(user.name)
			var gp = GradientPaint(
					0.0f, 0.0f,
                    Color(202, 72, 15),
					0.0f, fontMetrics.getHeight().toFloat() + fontMetrics.getHeight().toFloat(),
					Color(66, 181, 33));
			graphics.paint = gp;

			ImageUtils.drawCenteredStringOutlined(graphics, friendshipEnded, Rectangle(0, 10, 400, 30), font);
			graphics.color = Color.RED

			font = font.deriveFont(30F);
			graphics.font = font

			ImageUtils.drawCenteredStringOutlined(graphics, context.locale.AMIZADE_ENDED.f(), Rectangle(0, 30, 400, 40), font);

			font = font.deriveFont(24F)
			graphics.font = font
			fontMetrics = graphics.getFontMetrics(font)
			gp = GradientPaint(
					0.0f, 140f,
					Color(206, 7, 129),
					0.0f, 190f,
					Color(103, 216, 11));
			graphics.paint = gp;
			// graphics.fillRect(0, 0, 400, 300); // debugging
			ImageUtils.drawCenteredStringOutlined(graphics, "${context.locale.AMIZADE_NOW.f()} " + user2.name, Rectangle(0, 100, 400, 110), font);
			ImageUtils.drawCenteredStringOutlined(graphics, "${context.locale.AMIZADE_IS_MY.f()}", Rectangle(0, 120, 400, 130), font);
			graphics.color = Color.MAGENTA
			ImageUtils.drawCenteredStringOutlined(graphics, context.locale.AMIZADE_BEST_FRIEND.f(), Rectangle(0, 140, 400, 150), font);

			context.sendFile(template, "rip_amizade.png", context.getAsMention(true));
		} else {
			context.explain()
		}
	}
}