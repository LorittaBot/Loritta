package net.perfectdreams.loritta.commands.economy

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.tables.Raspadinhas
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class ScratchCardTopCommand : LorittaCommand(arrayOf("scratchcard top", "raspadinha top"), CommandCategory.ECONOMY) {
	override fun getDescription(locale: BaseLocale): String? {
		return locale["commands.economy.scratchcardtop.description"]
	}

	override fun getExamples(locale: BaseLocale): List<String> {
		return listOf()
	}

	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.NUMBER) {
				optional = true
			}
		}
	}

	@Subcommand
	suspend fun run(context: DiscordCommandContext, locale: BaseLocale) {
		var page = context.args.getOrNull(0)?.toIntOrNull()

		if (page != null)
			page -= 1

		if (page == null)
			page = 0

		val userId = Raspadinhas.receivedById
		val ticketCount = Raspadinhas.receivedById.count()
		val moneySum = Raspadinhas.value.sum()

		val userData = transaction(Databases.loritta) {
			Raspadinhas.slice(userId, ticketCount, moneySum)
					.selectAll()
					.groupBy(userId)
					.having {
						moneySum.isNotNull()
					}
					.orderBy(moneySum, SortOrder.DESC)
					.limit(5, page * 5)
					.toMutableList()
		}

		val rankHeader = ImageIO.read(File(Loritta.ASSETS, "rank_header.png"))
		val base = BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB_PRE)
		val graphics = base.graphics.enableFontAntiAliasing()

		val guildIconUrl = context.discordGuild!!.iconUrl

		val serverIconUrl = if (guildIconUrl != null) {
			guildIconUrl.replace("jpg", "png")
		} else {
			"${com.mrpowergamerbr.loritta.utils.loritta.instanceConfig.loritta.website.url}assets/img/unknown.png"
		}

		val serverIcon = (LorittaUtils.downloadImage(serverIconUrl) ?: Constants.DEFAULT_DISCORD_BLUE_AVATAR)
				.getScaledInstance(141, 141, BufferedImage.SCALE_SMOOTH)

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
		var currentY = 37

		for (profile in userData) {
			if (idx >= 5)
				break

			val userId = profile[userId].toString()
			val member = lorittaShards.retrieveUserInfoById(userId.toLong())

			if (member != null) {
				val rankBackground = loritta.getUserProfileBackground(member.id)
				graphics.drawImage(rankBackground.getScaledInstance(400, 300, BufferedImage.SCALE_SMOOTH)
						.toBufferedImage()
						.getSubimage(0, idx * 52, 400, 53), 0, currentY, null)

				graphics.color = Color(0, 0, 0, 127)
				graphics.fillRect(0, currentY, 400, 53)

				graphics.color = Color(255, 255, 255)

				graphics.font = oswaldRegular20

				ImageUtils.drawTextWrap(member.name, 143, currentY + 21, 9999, 9999, graphics.fontMetrics, graphics)

				graphics.font = oswaldRegular16

				ImageUtils.drawTextWrap(locale["commands.economy.scratchcardtop.wonTickets", profile[moneySum].toString(), profile[ticketCount].toString()], 144, currentY + 38, 9999, 9999, graphics.fontMetrics, graphics)

				graphics.font = oswaldRegular10

				// ImageUtils.drawTextWrap("Nível " + userData.getCurrentLevel().currentLevel, 145, currentY + 48, 9999, 9999, graphics.fontMetrics, graphics)

				val avatar = (LorittaUtils.downloadImage(member.effectiveAvatarUrl) ?: Constants.DEFAULT_DISCORD_BLUE_AVATAR)
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
		return
	}
}