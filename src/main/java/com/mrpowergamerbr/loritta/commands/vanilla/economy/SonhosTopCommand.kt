package com.mrpowergamerbr.loritta.commands.vanilla.economy

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class SonhosTopCommand : AbstractCommand("sonhostop", listOf("topsonhos"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["RANK_DESCRIPTION"]
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var page = context.args.getOrNull(0)?.toIntOrNull()

		if (page != null)
			page -= 1

		if (page == null)
			page = 0

		val userData = loritta.usersColl
				.find(Filters.gt("dreams", 60000))
				.sort(Sorts.descending("dreams"))
				.skip(page * 10)
				.limit(10)

		val list = userData.toMutableList()

		val rankHeader = ImageIO.read(File(Loritta.ASSETS, "rank_header.png"))
		val base = BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB_PRE)
		val graphics = base.graphics as Graphics2D

		graphics.setRenderingHint(
				java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
				java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

		val serverIconUrl = if (context.guild.iconUrl != null) {
			context.guild.iconUrl.replace("jpg", "png")
		} else {
			"${Loritta.config.websiteUrl}assets/img/unknown.png"
		}

		val serverIcon = LorittaUtils.downloadImage(serverIconUrl).getScaledInstance(141, 141, BufferedImage.SCALE_SMOOTH)

		graphics.drawImage(serverIcon, 259, -52, null)

		graphics.drawImage(rankHeader, 0, 0, null)

		val oswaldRegular10 = Constants.OSWALD_REGULAR
				.deriveFont(10F)

		val oswaldRegular16 = oswaldRegular10
				.deriveFont(16F)

		val oswaldRegular20 = oswaldRegular10
				.deriveFont(20F)

		graphics.font = oswaldRegular16

		ImageUtils.drawCenteredString(graphics, "Ranking Global", Rectangle(0, 0, 268, 37), oswaldRegular16)

		var idx = 0
		var currentY = 37;

		for (profile in list) {
			if (idx >= 5) {
				break
			}

			val member = lorittaShards.getUserById(profile.userId)

			if (member != null) {
				val file = java.io.File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + profile.userId + ".png")
				val imageFile = if (file.exists()) file else java.io.File(Loritta.FRONTEND, "static/assets/img/backgrounds/default_background.png")

				val rankBackground = ImageIO.read(imageFile)
				graphics.drawImage(rankBackground.getScaledInstance(400, 300, BufferedImage.SCALE_SMOOTH)
						.toBufferedImage()
						.getSubimage(0, idx * 52, 400, 53), 0, currentY, null)

				graphics.color = Color(0, 0, 0, 127)
				graphics.fillRect(0, currentY, 400, 53)

				graphics.color = Color(255, 255, 255)

				graphics.font = oswaldRegular20

				ImageUtils.drawTextWrap(member.name, 143, currentY + 21, 9999, 9999, graphics.fontMetrics, graphics)

				graphics.font = oswaldRegular16

				ImageUtils.drawTextWrap("${profile.dreams} sonhos", 144, currentY + 38, 9999, 9999, graphics.fontMetrics, graphics)

				graphics.font = oswaldRegular10

				// ImageUtils.drawTextWrap("Nível " + userData.getCurrentLevel().currentLevel, 145, currentY + 48, 9999, 9999, graphics.fontMetrics, graphics)

				val avatar = LorittaUtils.downloadImage(member.effectiveAvatarUrl)
						.getScaledInstance(143, 143, BufferedImage.SCALE_SMOOTH)

				var editedAvatar = BufferedImage(143, 143, BufferedImage.TYPE_INT_ARGB)
				val avatarGraphics = editedAvatar.graphics as Graphics2D

				val path = Path2D.Double()
				path.moveTo(0.0, 45.0)
				path.lineTo(132.0, 45.0)
				path.lineTo(143.0, 98.0)
				path.lineTo(0.0, 98.0)
				path.closePath()

				avatarGraphics.clip = path

				avatarGraphics.drawImage(avatar, 0, 0, null)

				editedAvatar = editedAvatar.getSubimage(0, 45, 143, 53)
				graphics.drawImage(editedAvatar, 0, currentY, null)
				idx++
				currentY += 53;
			}
		}
		context.sendFile(base.makeRoundedCorners(15), "rank.png", context.getAsMention(true))
	}
}