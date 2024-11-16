package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.cinnamon.discord.utils.images.*
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.geom.Path2D
import java.awt.image.BufferedImage

object RankingGenerator {
	val VALID_RANKING_PAGES = 1L..1000L

	/**
	 * Generates a ranking image
	 */
	suspend fun generateRanking(
		loritta: LorittaBot,
		currentPosition: Long,
		title: String,
		guildIconUrl: String?,
		rankedUsers: List<UserRankInformation>,
		onNullUser: (suspend (Long) -> (CachedUserInfo?))? = null
	): BufferedImage {
		val rankHeader = readImageFromResources("/rank/rank_header.png")
		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB_PRE)
		val graphics = base.createGraphics()
			.withTextAntialiasing()

		val serverIcon = (guildIconUrl?.let { ImageUtils.downloadImage(it) } ?: ImageUtils.DEFAULT_DISCORD_AVATAR)
			.getResizedInstance(282, 282, InterpolationType.BILINEAR)

		graphics.drawImage(serverIcon, 518, -104, null)

		graphics.drawImage(rankHeader.getScaledInstance(800, 74, BufferedImage.SCALE_SMOOTH), 0, 0, null)

		val oswaldRegular10 = loritta.graphicsFonts.oswaldRegular.deriveFont(20F)
		val oswaldRegular16 = loritta.graphicsFonts.oswaldRegular.deriveFont(32F)
		val oswaldRegular20 = loritta.graphicsFonts.oswaldRegular.deriveFont(40F)
		val badgeTitleFont = loritta.graphicsFonts.oswaldRegular.deriveFont(24f)
		val profileSubtitleFont = loritta.graphicsFonts.oswaldRegular.deriveFont(28f)

		graphics.font = oswaldRegular16

		ImageUtils.drawCenteredString(loritta, graphics, title, Rectangle(0, 0, 536, 74), oswaldRegular16, ImageUtils.ALLOWED_UNICODE_DRAWABLE_TYPES)

		var idx = 0
		var currentY = 74

		for (profile in rankedUsers) {
			if (idx >= 5) {
				break
			}

			val member = loritta.lorittaShards.retrieveUserInfoById(profile.userId) ?: onNullUser?.invoke(profile.userId)

			if (member != null) {
				val rankBackground = loritta.profileDesignManager.getUserProfileBackground(member.id, ProfileDesign.DEFAULT_PROFILE_DESIGN_ID)
				graphics.drawImage(rankBackground.getResizedInstance(800, 600, InterpolationType.BILINEAR)
					.getSubimage(0, idx * 104, 800, 106), 0, currentY, null)

				graphics.color = Color(0, 0, 0, 127)
				graphics.fillRect(0, currentY, 800, 106)

				graphics.color = Color(255, 255, 255)

				graphics.font = oswaldRegular20

				ImageUtils.drawString(loritta, graphics, "#${currentPosition + idx + 1} ${member.name}", 288, currentY + 37, ImageUtils.ALLOWED_UNICODE_DRAWABLE_TYPES)

				graphics.font = badgeTitleFont

				var renderedBadge = false

				val (userProfile, profileSettings, activeBadgeId) = loritta.newSuspendedTransaction {
					val profile = loritta._getLorittaProfile(member.id)
					val profileSettings = profile?.settings
					val activeBadge = profileSettings?.activeBadge
					Triple(profile, profileSettings, activeBadge)
				}

				if (userProfile != null && profileSettings != null) {
					// We need to query the user's badge to check if they still have their badge, instead of equipping a badge that they may not have anymore
					val badges = loritta.profileDesignManager.getUserBadges(
						loritta.profileDesignManager.transformUserToProfileUserInfoData(member, profileSettings),
						userProfile,
						setOf() // We don't care about mutual guilds badges since users cannot equip guild badges anyway
					)

					val activeBadge = badges.firstOrNull { it.id == activeBadgeId }

					if (activeBadge != null) {
						val badgeImage = activeBadge.getImage()

						if (badgeImage != null) {
							graphics.drawImage(
								badgeImage.getScaledInstance(24, 24, BufferedImage.SCALE_SMOOTH).toBufferedImage(),
								288,
								currentY + 40,
								null
							)
						}

						// Show the user's ID in badge title
						ImageUtils.drawString(
							loritta,
							graphics,
							loritta.languageManager.defaultI18nContext.get(activeBadge.title) + " // ID: ${userProfile.userId}",
							288 + 28,
							currentY + 40 + 22,
							ImageUtils.ALLOWED_UNICODE_DRAWABLE_TYPES
						)

						renderedBadge = true
					}
				}

				if (!renderedBadge) {
					// Show the user's ID in badge title
					ImageUtils.drawString(
						loritta,
						graphics,
						"ID: ${profile.userId}",
						288,
						currentY + 40 + 22,
						ImageUtils.ALLOWED_UNICODE_DRAWABLE_TYPES
					)
				}

				if (profile.subtitle != null) {
					graphics.font = profileSubtitleFont
					ImageUtils.drawString(loritta, graphics, profile.subtitle, 288, currentY + 96, ImageUtils.ALLOWED_UNICODE_DRAWABLE_TYPES)
				}

				graphics.font = oswaldRegular10

				val userAvatar = member.getEffectiveAvatarUrl(ImageFormat.PNG)
				val avatar = (ImageUtils.downloadImage(userAvatar) ?: ImageUtils.DEFAULT_DISCORD_AVATAR).getResizedInstance(286, 286, InterpolationType.BILINEAR)

				var editedAvatar = BufferedImage(286, 286, BufferedImage.TYPE_INT_ARGB)
				val avatarGraphics = editedAvatar.graphics as Graphics2D

				val path = Path2D.Double()
				path.moveTo(0.0, 90.0)
				path.lineTo(264.0, 90.0)
				path.lineTo(286.0, 196.0)
				path.lineTo(0.0, 196.0)
				path.closePath()

				avatarGraphics.clip = path

				avatarGraphics.drawImage(avatar, 0, 0, null)

				editedAvatar = editedAvatar.getSubimage(0, 90, 286, 106)
				graphics.drawImage(editedAvatar, 0, currentY, null)
				idx++
				currentY += 106
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
		val subtitle: String? = null
	)
}