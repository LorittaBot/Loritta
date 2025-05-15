package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.images.InterpolationType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.getResizedInstance
import net.perfectdreams.loritta.cinnamon.discord.utils.images.withTextAntialiasing
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ProfileDesign
import java.awt.Color
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.image.BufferedImage

object RankingGenerator {
	val VALID_RANKING_PAGES = 1L..1000L
	private const val HEADER_HEIGHT = 70
	private const val SERVER_ICON_SIZE = 70 - 4 - 4
	private const val ENTRIES_START_Y = HEADER_HEIGHT
	private val HEADER_COLOR = Color(30, 33, 36)
	private val BACKGROUND_COLOR = Color(18, 18, 20)
	private val GRADIENT_LEFT_COLOR =  Color(0, 0, 10, 190) // a tiny bit of blue!
	private val GRADIENT_RIGHT_COLOR = Color(0, 0, 0, 150)
	private const val MAXIMUM_RENDERABLE_ENTRIES = 5
	private const val ENTRY_HEIGHT = 106

	/**
	 * Draws the gradient used on each of the entries in the ranking
	 *
	 * @param image the source image
	 */
	fun drawEntryBackgroundGradient(image: BufferedImage) {
		val graphics = image.createGraphics()

		// The gradient goes from (entryDrawX, entryDrawY) to (entryDrawX + entryWidth, entryDrawY)
		val gradient = GradientPaint(
			0f,
			0f, // Y-coordinate for the gradient line start
			GRADIENT_LEFT_COLOR,
			(800 / 2).toFloat(),
			0f, // Y-coordinate for the gradient line end (same for horizontal)
			GRADIENT_RIGHT_COLOR
		)

		graphics.paint = gradient // Apply the gradient

		// Fill the area
		graphics.fillRect(
			0,
			0,
			image.width,
			image.height
		)

		graphics.dispose()
	}

	/**
	 * Generates a ranking image of a user list
	 *
	 * @param loritta         loritta
	 * @param currentPosition the position of the first user of the list in the ranking
	 * @param title           the title of the ranking
	 * @param guildIconUrl    the icon URL of the guild
	 * @param rankedUsers     the list of users that will be rendered on the ranking
	 * @param onNullUser      a function that is called when a user is null
	 * @return the rank image
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

		var idx = 0
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

				val rankBackground = loritta.profileDesignManager.getUserProfileBackground(member.id, ProfileDesign.DEFAULT_PROFILE_DESIGN_ID)

				val userBackgroundSectionPart = rankBackground.getResizedInstance(800, 600, InterpolationType.BILINEAR)
					.getSubimage(
						0, ENTRIES_START_Y + (idx * ENTRY_HEIGHT), 800,
						ENTRY_HEIGHT
					)

				val userBackgroundSectionPartBase = BufferedImage(
					userBackgroundSectionPart.width,
					userBackgroundSectionPart.height,
					BufferedImage.TYPE_INT_ARGB
				)
				val userBackgroundSectionPartBaseGraphics = userBackgroundSectionPartBase.createGraphics()
				userBackgroundSectionPartBaseGraphics.drawImage(userBackgroundSectionPart, 0, 0, null)

				drawEntryBackgroundGradient(userBackgroundSectionPartBase)

				entries.add(
					EntryRankInformation(
						member.globalName ?: member.name,
						iconableSubtitle,
						userRankInformation.subtitle,
						member.getEffectiveAvatarUrl(ImageFormat.PNG).let { url -> ImageUtils.downloadImage(url) ?: ImageUtils.DEFAULT_DISCORD_AVATAR },
						userBackgroundSectionPartBase
					)
				)

				idx++
			}
		}

		return generateRanking(
			loritta,
			currentPosition,
			title,
			guildIconUrl,
			null,
			entries
		)
	}

	/**
	 * Generates a ranking image of a entry list
	 *
	 * @param loritta         loritta
	 * @param currentPosition the position of the first entry of the list in the ranking
	 * @param title           the title of the ranking
	 * @param guildIconUrl    the icon URL of the guild
	 * @param background      the background that will be displayed
	 * @param rankedEntries   the list of entries that will be rendered on the ranking
	 * @return the rank image
	 */
	suspend fun generateRanking(
		loritta: LorittaBot,
		currentPosition: Long,
		title: String,
		guildIconUrl: String?,
		background: BufferedImage?,
		rankedEntries: List<EntryRankInformation>
	): BufferedImage {
		// Validate if all entries are valid
		// In theory we DO NOT need to filter this, but we do have this check to check if any of the commands are querying way too many values and passing thru here
		if (rankedEntries.size > MAXIMUM_RENDERABLE_ENTRIES)
			error("Rendering too many entries! You can only render a maximum of $MAXIMUM_RENDERABLE_ENTRIES entries per ranking! Count: ${rankedEntries.size}")

		for (entry in rankedEntries) {
			if (entry.background != null && (entry.background.width != 800 || entry.background.height != ENTRY_HEIGHT))
				error("Background size of entry ${entry.name} is invalid! The background must be 800x${ENTRY_HEIGHT}! Current size: ${entry.background.width}x${entry.background.height}")
		}

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

		if (background != null)
			graphics.drawImage(background, 0, 0, null)

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
			val textOffsetX = 98 + 24 + 16

			val rankBackground = entry.background

			val targetX = 0
			val targetY = (idx * 106) + ENTRIES_START_Y

			if (rankBackground != null) {
				graphics.drawImage(
					rankBackground,
					targetX,
					targetY,
					null
				)
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
			if (userAvatar != null) {
				val userAvatarBase = BufferedImage(98, 98, BufferedImage.TYPE_INT_ARGB)
				val userAvatarBaseGraphics = userAvatarBase.createGraphics()

				userAvatarBaseGraphics.color = HEADER_COLOR
				userAvatarBaseGraphics.fillRect(0, 0, userAvatarBase.width, userAvatarBase.height)
				userAvatarBaseGraphics.drawImage(
					userAvatar.getResizedInstance(
						userAvatarBase.width,
						userAvatarBase.height,
						InterpolationType.BILINEAR
					), 0, 0, null
				)

				graphics.drawImage(userAvatarBase.makeRoundedCorners(userAvatarBase.width), 24, currentY + 4, null)
			}

			idx++
			currentY += 106
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
		val icon: BufferedImage?,
		val background: BufferedImage?
	) {
		data class EntryRankIconableSubtitle(
			val icon: BufferedImage?,
			val text: String
		)
	}
}