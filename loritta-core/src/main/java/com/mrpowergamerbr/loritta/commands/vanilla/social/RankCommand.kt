package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.CommandCategory
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class RankCommand : AbstractCommand("rank", listOf("top", "leaderboard", "ranking"), CommandCategory.SOCIAL) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["RANK_DESCRIPTION"]
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		var global = false
		var page = context.args.getOrNull(0)?.toIntOrNull()

		if (page != null)
			page -= 1

		if (page == null)
			page = 0

		val profiles = transaction(Databases.loritta) {
			GuildProfiles.select { GuildProfiles.guildId eq context.guild.idLong }
					.orderBy(GuildProfiles.xp to false)
					.limit(5, page * 5)
					.toMutableList()
		}

		logger.trace { "Retrived profiles of ${context.guild}"}
		logger.trace { "profiles.size = ${profiles.size}"}

		val rankHeader = ImageIO.read(File(Loritta.ASSETS, "rank_header.png"))
		val base = BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB_PRE)
		val graphics = base.graphics.enableFontAntiAliasing()

		val guildIconUrl = context.guild.iconUrl
		val serverIconUrl = if (guildIconUrl != null) {
			guildIconUrl.replace("jpg", "png")
		} else {
			"${loritta.instanceConfig.loritta.website.url}assets/img/unknown.png"
		}

		val serverIcon = LorittaUtils.downloadImage(serverIconUrl)!!.getScaledInstance(141, 141, BufferedImage.SCALE_SMOOTH)

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
		var currentY = 37

		for (resultRow in profiles) {
			if (idx >= 5) {
				break
			}

			val profile = transaction(Databases.loritta) { GuildProfile.wrapRow(resultRow) }
			val member = context.guild.getMemberById(profile.userId)

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

				ImageUtils.drawTextWrap(member.user.name, 143, currentY + 21, 9999, 9999, graphics.fontMetrics, graphics)

				graphics.font = oswaldRegular16

				ImageUtils.drawTextWrap("XP total // " + profile.xp, 144, currentY + 38, 9999, 9999, graphics.fontMetrics, graphics)

				graphics.font = oswaldRegular10

				ImageUtils.drawTextWrap("Nível " + profile.getCurrentLevel().currentLevel, 145, currentY + 48, 9999, 9999, graphics.fontMetrics, graphics)

				val avatar = (LorittaUtils.downloadImage(member.user.effectiveAvatarUrl) ?: LorittaUtils.downloadImage("https://loritta.website/assets/img/unknown.png")!!)
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
				currentY += 53
			}
		}
		context.sendFile(base.makeRoundedCorners(15), "rank.png", context.getAsMention(true))
	}
}