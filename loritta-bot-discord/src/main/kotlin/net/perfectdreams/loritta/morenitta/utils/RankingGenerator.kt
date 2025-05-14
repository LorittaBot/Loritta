package net.perfectdreams.loritta.morenitta.utils

import kotlinx.html.HEADER
import net.perfectdreams.loritta.cinnamon.discord.utils.images.*
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.DrawableType
import net.perfectdreams.loritta.cinnamon.discord.utils.toJavaColor
import net.perfectdreams.loritta.common.utils.LorittaColors
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import java.awt.Color
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.image.BufferedImage

object RankingGenerator {
	val VALID_RANKING_PAGES = 1L..1000L
	private const val HEADER_HEIGHT = 60
	private const val SERVER_ICON_SIZE = 60 - 4 - 4
	private const val ENTRIES_START_Y = HEADER_HEIGHT + 10
	private val HEADER_COLOR = Color(30, 33, 36)
	private val BACKGROUND_COLOR = Color(18, 18, 20)
	private val GRADIENT_LEFT_COLOR =  Color(0, 0, 10, 190) // a tiny bit of blue!
	private val GRADIENT_RIGHT_COLOR = Color(0, 0, 0, 150)

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
		// We need to convert our UserRankInformation to EntryRankInformation
		val entries = mutableListOf<EntryRankInformation>()

		for (userRankInformation in rankedUsers) {
			val member = loritta.lorittaShards.retrieveUserInfoById(userRankInformation.userId) ?: onNullUser?.invoke(userRankInformation.userId)

			if (member != null) {
				val (userProfile, profileSettings, activeBadgeId) = loritta.newSuspendedTransaction {
					val profile = loritta._getLorittaProfile(member.id)
					val profileSettings = profile?.settings
					val activeBadge = profileSettings?.activeBadge
					Triple(profile, profileSettings, activeBadge)
				}

				var iconableSubtitle = EntryRankInformation.EntryRankIconableSubtitle(null, "ID: ${member.id}")

				if (userProfile != null && profileSettings != null) {
					// We need to query the user's badge to check if they still have their badge, instead of equipping a badge that they may not have anymore
					val badges = loritta.profileDesignManager.getUserBadges(
						loritta.profileDesignManager.transformUserToProfileUserInfoData(
							member,
							profileSettings
						),
						userProfile,
						setOf() // We don't care about mutual guilds badges since users cannot equip guild badges anyway
					)

					val activeBadge = badges.firstOrNull { it.id == activeBadgeId }

					if (activeBadge != null) {
						val badgeImage = activeBadge.getImage()
						iconableSubtitle = EntryRankInformation.EntryRankIconableSubtitle(
							badgeImage,
							loritta.languageManager.defaultI18nContext.get(activeBadge.title) + " // ID: ${userProfile.userId}"
						)
					}
				}

				entries.add(
					EntryRankInformation(
						member.globalName ?: member.name,
						iconableSubtitle,
						userRankInformation.subtitle,
						member.getEffectiveAvatarUrl(ImageFormat.PNG).let { url -> ImageUtils.downloadImage(url) ?: ImageUtils.DEFAULT_DISCORD_AVATAR },
						loritta.profileDesignManager.getUserProfileBackground(member.id, ProfileDesign.DEFAULT_PROFILE_DESIGN_ID)
					)
				)
			}
		}

		return generateRanking(
			loritta,
			currentPosition,
			title,
			guildIconUrl,
			entries
		)
	}

	/**
	 * Generates a ranking image
	 */
	suspend fun generateRanking(
		loritta: LorittaBot,
		currentPosition: Long,
		title: String,
		guildIconUrl: String?,
		rankedEntries: List<EntryRankInformation>
	): BufferedImage {
		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB_PRE)
		val graphics = base.createGraphics()
			.withTextAntialiasing()
		graphics.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON
		)
		graphics.setRenderingHint(
			RenderingHints.KEY_RENDERING,
			RenderingHints.VALUE_RENDER_QUALITY
		)

		suspend fun drawStringWithDropShadow(text: String, x: Int, y: Int) {
			val originalColor = graphics.color
			graphics.color = Color.BLACK

			ImageUtils.drawString(
				loritta, graphics,
				text,
				x,
				y + 2,
				ImageUtils.ALLOWED_UNICODE_DRAWABLE_TYPES
			)

			graphics.color = originalColor

			ImageUtils.drawString(
				loritta, graphics,
				text,
				x,
				y,
				ImageUtils.ALLOWED_UNICODE_DRAWABLE_TYPES
			)
		}

		graphics.color = BACKGROUND_COLOR
		graphics.fillRect(0, 0, 800, 600)

		graphics.color = HEADER_COLOR
		graphics.fillRect(0, 0, 800, HEADER_HEIGHT)

		if (guildIconUrl != null) {
			val serverIcon = (guildIconUrl.let { ImageUtils.downloadImage(it) } ?: ImageUtils.DEFAULT_DISCORD_AVATAR)
				.getResizedInstance(SERVER_ICON_SIZE, SERVER_ICON_SIZE, InterpolationType.BILINEAR)

			val serverIconBase = BufferedImage(serverIcon.width, serverIcon.height, BufferedImage.TYPE_INT_ARGB)
			val serverIconBaseGraphics = serverIconBase.createGraphics()

			serverIconBaseGraphics.color = HEADER_COLOR
			serverIconBaseGraphics.fillRect(0, 0, serverIconBase.width, serverIconBase.height)
			serverIconBaseGraphics.drawImage(serverIcon.getResizedInstance(serverIconBase.width, serverIconBase.height, InterpolationType.BILINEAR), 0, 0, null)

			// we don't round fully round because that's how Discord server icons look on the sidebar nowadays
			val roundedIcon = serverIcon.makeRoundedCorners(36)

			// right side icon
			graphics.drawImage(roundedIcon, 800 - 4 - SERVER_ICON_SIZE, 4, null)
			// left side icon
			// graphics.drawImage(roundedIcon, 4, 4, null)
		}

		val badgeTitleFont = loritta.graphicsFonts.oswaldRegular.deriveFont(24f)
		val profileSubtitleFont = loritta.graphicsFonts.oswaldRegular.deriveFont(28f)
		val rankTitleFont = loritta.graphicsFonts.latoBlack.deriveFont(38f)
		val userTitleFont = loritta.graphicsFonts.latoBlack.deriveFont(35f)

		graphics.color = Color.WHITE
		graphics.font = rankTitleFont

		ImageUtils.drawCenteredString(
			loritta,
			graphics,
			title,
			Rectangle(66 + 4 + 4, 0, 800 - ((4 + 66 + 4) * 2), HEADER_HEIGHT),
			rankTitleFont,
			ImageUtils.ALLOWED_UNICODE_DRAWABLE_TYPES
		)

		var idx = 0
		var currentY = ENTRIES_START_Y

		for (entry in rankedEntries) {
			if (idx >= 5) {
				break
			}

			val textOffsetX = 98 + 24 + 16

			val rankBackground = entry.background

			if (true) {
				val sourceX = 0
				// If it is the first entry, we need to get a lil bit more of the background due to the triangle flair
				val sourceY = (idx * 106) + if (idx == 0)
					HEADER_HEIGHT
				else
					ENTRIES_START_Y

				val userBackgroundSectionPart = rankBackground.getResizedInstance(800, 600, InterpolationType.BILINEAR)
					.getSubimage(sourceX, sourceY, 800,
						if (idx == 0)
							106 + (ENTRIES_START_Y - HEADER_HEIGHT)
						else
							106
					)

				val userBackgroundSectionPartBase = BufferedImage(userBackgroundSectionPart.width, userBackgroundSectionPart.height, BufferedImage.TYPE_INT_ARGB)
				val userBackgroundSectionPartBaseGraphics = userBackgroundSectionPartBase.createGraphics()
				userBackgroundSectionPartBaseGraphics.drawImage(userBackgroundSectionPart, 0, 0, null)

				val originalPaint = userBackgroundSectionPartBaseGraphics.paint // Store original paint

				// Create a GradientPaint object
				// The gradient goes from (entryDrawX, entryDrawY) to (entryDrawX + entryWidth, entryDrawY)
				val gradient = GradientPaint(
					0f,
					currentY.toFloat(), // Y-coordinate for the gradient line start
					GRADIENT_LEFT_COLOR,
					(800 / 2).toFloat(),
					currentY.toFloat(), // Y-coordinate for the gradient line end (same for horizontal)
					GRADIENT_RIGHT_COLOR
				)

				userBackgroundSectionPartBaseGraphics.paint = gradient // Apply the gradient
				userBackgroundSectionPartBaseGraphics.fillRect(0, 0, userBackgroundSectionPartBase.width, userBackgroundSectionPartBase.height) // Fill the area

				graphics.drawImage(
					userBackgroundSectionPartBase,
					sourceX,
					sourceY,
					null
				)

				graphics.paint = originalPaint // Restore original paint
			} else {
				graphics.drawImage(
					rankBackground.getResizedInstance(800, 600, InterpolationType.BILINEAR)
						.getSubimage(0, idx * 104, 800, 106), 0, currentY, null
				)

				val originalPaint = graphics.paint // Store original paint

				// Create a GradientPaint object
				// The gradient goes from (entryDrawX, entryDrawY) to (entryDrawX + entryWidth, entryDrawY)
				val gradient = GradientPaint(
					0f,
					currentY.toFloat(), // Y-coordinate for the gradient line start
					GRADIENT_LEFT_COLOR,
					(800 / 2).toFloat(),
					currentY.toFloat(), // Y-coordinate for the gradient line end (same for horizontal)
					GRADIENT_RIGHT_COLOR
				)

				graphics.paint = gradient // Apply the gradient
				graphics.fillRect(0, currentY, 800, 106) // Fill the area

				graphics.paint = originalPaint // Restore original paint
			}

			graphics.color = Color(255, 255, 255)

			graphics.font = userTitleFont

			drawStringWithDropShadow("#${currentPosition + idx + 1} ${entry.name}", textOffsetX, currentY + 37)

			graphics.font = badgeTitleFont

			val subtitle = entry.iconableSubtitle

			if (subtitle != null) {
				val subtitleIcon = subtitle.icon

				if (subtitleIcon != null) {
					graphics.drawImage(
						subtitleIcon.getScaledInstance(24, 24, BufferedImage.SCALE_SMOOTH).toBufferedImage(),
						textOffsetX,
						currentY + 42,
						null
					)
				}

				drawStringWithDropShadow(
					entry.iconableSubtitle.text,
					if (subtitleIcon != null) textOffsetX + 28 else textOffsetX,
					currentY + 42 + 22
				)
			}

			if (entry.subtitle != null) {
				graphics.font = profileSubtitleFont
				drawStringWithDropShadow(entry.subtitle, textOffsetX, currentY + 96)
			}

			// Make the background of the icon the same color as the background of the header
			// (mostly for transparent icons)
			val userAvatar = entry.icon
			val userAvatarBase = BufferedImage(98, 98, BufferedImage.TYPE_INT_ARGB)
			val userAvatarBaseGraphics = userAvatarBase.createGraphics()

			userAvatarBaseGraphics.color = HEADER_COLOR
			userAvatarBaseGraphics.fillRect(0, 0, userAvatarBase.width, userAvatarBase.height)
			userAvatarBaseGraphics.drawImage(userAvatar.getResizedInstance(userAvatarBase.width, userAvatarBase.height, InterpolationType.BILINEAR), 0, 0, null)

			graphics.drawImage(userAvatarBase.makeRoundedCorners(userAvatarBase.width), 24, currentY + 4, null)

			idx++
			currentY += 106
		}

		// The triangles below the header
		graphics.color = HEADER_COLOR

		repeat(base.width / 25) {
			val offsetX = it * 25

			graphics.fillPolygon(
				intArrayOf(0 + offsetX, 13 + offsetX, 25 + offsetX),
				intArrayOf(HEADER_HEIGHT, HEADER_HEIGHT + 10, HEADER_HEIGHT),
				3
			)
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

	data class EntryRankInformation(
		val name: String,
		val iconableSubtitle: EntryRankIconableSubtitle?,
		val subtitle: String?,
		val icon: BufferedImage,
		val background: BufferedImage
	) {
		data class EntryRankIconableSubtitle(
			val icon: BufferedImage?,
			val text: String
		)
	}
}