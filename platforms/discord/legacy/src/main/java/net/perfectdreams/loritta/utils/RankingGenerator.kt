package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.*
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.io.File

object RankingGenerator {
	/**
	 * Generates a ranking image
	 */
	suspend fun generateRanking(
			title: String,
			guildIconUrl: String?,
			rankedUsers: List<UserRankInformation>,
			onNullUser: (suspend (Long) -> (CachedUserInfo?))? = null
	): BufferedImage {
		val rankHeader = readImage(File(Loritta.ASSETS, "rank_header.png"))
		val base = BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB_PRE)
		val graphics = base.graphics.enableFontAntiAliasing()

		val serverIconUrl = if (guildIconUrl != null) {
			guildIconUrl.replace("jpg", "png")
		} else {
			"${loritta.instanceConfig.loritta.website.url}assets/img/unknown.png"
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

		ImageUtils.drawCenteredString(graphics, title, Rectangle(0, 0, 268, 37), oswaldRegular16)

		var idx = 0
		var currentY = 37

		for (profile in rankedUsers) {
			if (idx >= 5) {
				break
			}

			val member = lorittaShards.retrieveUserInfoById(profile.userId) ?: onNullUser?.invoke(profile.userId)

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

				if (profile.subtitle != null)
					ImageUtils.drawTextWrap(profile.subtitle, 144, currentY + 38, 9999, 9999, graphics.fontMetrics, graphics)

				graphics.font = oswaldRegular10

				// Show the user's ID in the subsubtitle
				ImageUtils.drawTextWrap((profile.subsubtitle?.let { "$it // " } ?: "") + "ID: ${profile.userId}", 145, currentY + 48, 9999, 9999, graphics.fontMetrics, graphics)

				val avatar = (
						LorittaUtils.downloadImage(
								member.getEffectiveAvatarUrl(ImageFormat.PNG)
						) ?: Constants.DEFAULT_DISCORD_BLUE_AVATAR
						).getScaledInstance(143, 143, BufferedImage.SCALE_SMOOTH)

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
		return base
	}

	/**
	 * Checks if the user is trying to retrieve a valid ranking page
	 *
	 * To avoid overloading the database with big useless ranking queries, we only allow
	 * pages from 1 to 100 to be retrieved
	 *
	 * @param input the page input
	 * @return if the input is in a valid range
	 */
	suspend fun isValidRankingPage(input: Long) = input in 1..100

	data class UserRankInformation(
			val userId: Long,
			val subtitle: String? = null,
			val subsubtitle: String? = null
	)
}