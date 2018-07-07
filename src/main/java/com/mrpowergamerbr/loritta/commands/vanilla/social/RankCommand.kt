package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaGuildUserData
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.makeRoundedCorners
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import java.awt.*
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class RankCommand : AbstractCommand("rank", listOf("top", "leaderboard", "ranking"), CommandCategory.SOCIAL) {
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
		val list = mutableListOf<RankWrapper>()

		var global = false
		var page = context.args.getOrNull(0)?.toIntOrNull()

		if (page != null)
			page -= 1

		if (page == null)
			page = 0

		if (!global) {
			context.config.guildUserData
					.forEach { list.add(RankWrapper(it.userId, it)) }
		}

		list.sortBy { it.userData.xp }
		list.reverse()

		for (idx in 0 until (page * 5)) {
			if (list.size >= 5)
				list.removeAt(0)
		}

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

		ImageUtils.drawCenteredString(graphics, if (global) "Ranking Global" else context.guild.name, Rectangle(0, 0, 268, 37), oswaldRegular16)

		var idx = 0
		var currentY = 37;

		for ((id, userData) in list) {
			if (idx >= 5) {
				break
			}

			var member = lorittaShards.getUserById(id)

			if (member != null) {
				val userProfile = loritta.getLorittaProfileForUser(id)
				val file = java.io.File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + userProfile.userId + ".png")
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

				ImageUtils.drawTextWrap("XP total // " + userData.xp, 144, currentY + 38, 9999, 9999, graphics.fontMetrics, graphics)

				graphics.font = oswaldRegular10

				ImageUtils.drawTextWrap("Nível " + userData.getCurrentLevel().currentLevel, 145, currentY + 48, 9999, 9999, graphics.fontMetrics, graphics)

				val avatar = LorittaUtils.downloadImage(member.effectiveAvatarUrl).getScaledInstance(143, 143, BufferedImage.SCALE_SMOOTH)

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

	data class RankWrapper(
			val id: String,
			val userData: LorittaGuildUserData)
}