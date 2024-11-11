package net.perfectdreams.loritta.morenitta.profile.profiles

import kotlinx.coroutines.runBlocking
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.images.InterpolationType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.getResizedInstance
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImageFromResources
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.loricoolcards.CardRarity
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.loricoolcards.LoriCoolCardsManager
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileGuildInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUtils
import net.perfectdreams.loritta.morenitta.utils.*
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

open class LoriCoolCardsStickerReceivedProfileCreator(
	loritta: LorittaBot,
	internalName: String,
	val rarity: CardRarity,
	val useLorittaBackground: Boolean
) : RawProfileCreator(loritta, internalName) {
	companion object {
		private const val PADDING = 8
	}

	class LoriCoolCardsStickerReceivedCommonProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedProfileCreator(loritta, "loriCoolCardsStickerReceivedCommon", CardRarity.COMMON, false)
	class LoriCoolCardsStickerReceivedUncommonProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedProfileCreator(loritta, "loriCoolCardsStickerReceivedUncommon", CardRarity.UNCOMMON, false)
	class LoriCoolCardsStickerReceivedRareProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedProfileCreator(loritta, "loriCoolCardsStickerReceivedRare", CardRarity.RARE, false)
	class LoriCoolCardsStickerReceivedEpicProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedProfileCreator(loritta, "loriCoolCardsStickerReceivedEpic", CardRarity.EPIC, false)
	class LoriCoolCardsStickerReceivedLegendaryProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedProfileCreator(loritta, "loriCoolCardsStickerReceivedLegendary", CardRarity.LEGENDARY, false)
	class LoriCoolCardsStickerReceivedMythicProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedProfileCreator(loritta, "loriCoolCardsStickerReceivedMythic", CardRarity.MYTHIC, false)
	class LoriCoolCardsStickerReceivedCommonUserBackgroundProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedProfileCreator(loritta, "loriCoolCardsStickerReceivedCommonUserBackground", CardRarity.COMMON, true)
	class LoriCoolCardsStickerReceivedUncommonUserBackgroundProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedProfileCreator(loritta, "loriCoolCardsStickerReceivedUncommonUserBackground", CardRarity.UNCOMMON, true)
	class LoriCoolCardsStickerReceivedRareUserBackgroundProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedProfileCreator(loritta, "loriCoolCardsStickerReceivedRareUserBackground", CardRarity.RARE, true)
	class LoriCoolCardsStickerReceivedEpicUserBackgroundProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedProfileCreator(loritta, "loriCoolCardsStickerReceivedEpicUserBackground", CardRarity.EPIC, true)
	class LoriCoolCardsStickerReceivedLegendaryUserBackgroundProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedProfileCreator(loritta, "loriCoolCardsStickerReceivedLegendaryUserBackground", CardRarity.LEGENDARY, true)
	class LoriCoolCardsStickerReceivedMythicUserBackgroundProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedProfileCreator(loritta, "loriCoolCardsStickerReceivedMythicUserBackground", CardRarity.MYTHIC, true)

	override suspend fun create(
		sender: ProfileUserInfoData,
		user: ProfileUserInfoData,
		userProfile: Profile,
		guild: ProfileGuildInfoData?,
		badges: List<BufferedImage>,
		badgesData: List<Badge>,
		equippedBadge: Badge?,
		locale: BaseLocale,
		i18nContext: I18nContext,
		background: BufferedImage,
		aboutMe: String,
		allowedDiscordEmojis: List<Long>?
	): Pair<ByteArray, ImageFormat> {
		val sectionTitleFont = loritta.graphicsFonts.latoBlack.deriveFont(20f)
		val sectionSubtitleFont = loritta.graphicsFonts.latoBold.deriveFont(20f)
		val marriageTimeSubtitleFont = loritta.graphicsFonts.latoBold.deriveFont(18f)
		val profileSonhosIconImage = readImageFromResources("/profile/loricoolcards_sticker_received/profile_sonhos_icon.png")
			.getResizedInstance(38, 38, InterpolationType.BILINEAR)
		val profileRepsIconImage = readImageFromResources("/profile/loricoolcards_sticker_received/profile_reps_icon.png")
			.getResizedInstance(38, 38, InterpolationType.BILINEAR)

		// TODO: Avatar fallback
		val avatar = ImageIO.read(URL(user.avatarUrl))
		val localProfile = if (guild != null) ProfileUtils.getLocalProfile(loritta, guild, user) else null
		val localPosition = ProfileUtils.getLocalExperiencePosition(loritta, localProfile)
		val globalEconomyPosition = ProfileUtils.getGlobalEconomyPosition(loritta, userProfile)
		val reputations = ProfileUtils.getReputationCount(loritta, user)
		val marriageInfo = ProfileUtils.getMarriageInfo(loritta, userProfile)
		val marriagePartnerAvatar = marriageInfo?.let { LorittaUtils.downloadImage(loritta, marriageInfo.partner.getEffectiveAvatarUrl(ImageFormat.PNG, 64))?.getResizedInstance(38, 38, InterpolationType.BILINEAR) }

		val frontFacingStickerImage = loritta.loriCoolCardsManager.generateFrontFacingSticker(
			LoriCoolCardsManager.CardGenData(
				"LorittaMorenitta",
				rarity,
				user.name,
				avatar,
				background,
				equippedBadge?.title?.let { i18nContext.get(it) },
				equippedBadge?.getImage()
			)
		)

		// While we could use 1280x720 (16:9) for the profile design, we will keep the good old 4:3 ratio
		return Pair(
			loritta.loriCoolCardsManager
				.generateStickerReceivedGIF(
					rarity,
					frontFacingStickerImage,
					LoriCoolCardsManager.StickerReceivedRenderType.ProfileDesignWithInfo(
						if (useLorittaBackground) {
							run {
								val scaledBackground = background.getScaledInstance(
									960,
									720,
									BufferedImage.SCALE_SMOOTH
								)

								return@run { graphics2d, cardX, _, imageRenderType ->
									graphics2d.drawImage(scaledBackground, 0, 0, null)
								}
							}
						} else {
							null
						}
					) { graphics2d, cardX, _, imageRenderType ->
						// The padding on everything here is 8px!!!
						graphics2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)
						graphics2d.setRenderingHint(
							RenderingHints.KEY_TEXT_ANTIALIASING,
							RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
						)

						// Draw the background info
						// The width is guesswork because of the sparkles
						graphics2d.fillRect(0, 0, cardX - 32, imageRenderType.height)

						graphics2d.fillRect(0, 0, 48 + PADDING + PADDING, imageRenderType.height)

						var badgeY = PADDING
						for (badgeImage in badges) {
							graphics2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
							graphics2d.drawImage(badgeImage, 8, badgeY, 48, 48, null)
							badgeY += 48 + PADDING
							graphics2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)
						}

						graphics2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
						graphics2d.color = Color.WHITE

						// This is weird as hell, isn't there's a way to make this EASIER TO UNDERSTAND???
						if (guild != null) {
							val guildIcon =
								(guild.iconUrl?.replace("jpg", "png")?.let { LorittaUtils.downloadImage(loritta, it) }
									?: Constants.MISSING_DISCORD_ICON_FALLBACK_IMAGE).getScaledInstance(
									38,
									38,
									BufferedImage.SCALE_SMOOTH
								)

							val xpLocal = localProfile?.xp

							val stringToBeDrawn = if (xpLocal != null) {
								if (localPosition != null) {
									"#$localPosition / $xpLocal XP"
								} else {
									"$xpLocal XP"
								}
							} else {
								"???"
							}

							graphics2d.font = sectionTitleFont
							graphics2d.drawText(loritta, guild.name, 114, PADDING + 17, 800 - 6)
							graphics2d.font = sectionSubtitleFont
							graphics2d.drawText(loritta, stringToBeDrawn, 114, PADDING + 17 + 17, 800 - 6)

							graphics2d.drawImage(guildIcon.toBufferedImage().makeRoundedCorners(38), 72, PADDING, null)
						}

						graphics2d.font = sectionTitleFont
						graphics2d.drawText(loritta, "Sonhos", 114, PADDING + 17 + 17 + PADDING + 17, 800 - 6)
						graphics2d.font = sectionSubtitleFont
						graphics2d.drawText(
							loritta,
							if (globalEconomyPosition != null)
								"#$globalEconomyPosition / ${userProfile.money}"
							else
								"${userProfile.money}",
							114,
							PADDING + 17 + 17 + PADDING + 17 + 17,
							800 - 6
						)

						graphics2d.drawImage(profileSonhosIconImage, 72, PADDING + 17 + 17 + PADDING, null)

						graphics2d.font = sectionTitleFont
						graphics2d.drawText(
							loritta,
							"Reputações",
							114,
							PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING + 17,
							800 - 6
						)
						graphics2d.font = sectionSubtitleFont
						graphics2d.drawText(
							loritta,
							reputations.toString(),
							114,
							PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING + 17 + 17,
							800 - 6
						)

						graphics2d.drawImage(
							profileRepsIconImage,
							72,
							PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING,
							null
						)

						if (marriageInfo != null) {
							graphics2d.font = sectionTitleFont
							graphics2d.drawText(
								loritta,
								"Casado com",
								114,
								PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING + 17,
								800 - 6
							)
							graphics2d.font = sectionSubtitleFont
							graphics2d.drawText(
								loritta,
								marriageInfo.partner.name ?: "???",
								114,
								PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING + 17 + 17,
								800 - 6
							)
							graphics2d.font = marriageTimeSubtitleFont
							graphics2d.drawText(
								loritta,
								DateUtils.formatDateDiff(
									i18nContext,
									marriageInfo.marriage.marriedSince,
									System.currentTimeMillis(),
									3
								),
								114,
								PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING + 17 + 17 + 17,
								800 - 6
							)

							if (marriagePartnerAvatar != null)
								graphics2d.drawImage(
									marriagePartnerAvatar.toBufferedImage().makeRoundedCorners(38),
									72,
									PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING,
									null
								)
						}


						graphics2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)
						graphics2d.color = Color.BLACK

						// TODO: Shift up or down the section depending if the user is married or not
						val w = (cardX - 32) - (48 + PADDING + PADDING) - PADDING - PADDING
						graphics2d.fillRect(
							48 + PADDING + PADDING + PADDING,
							PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING + 17 + 17 + 17 + PADDING,
							(cardX - 32) - (48 + PADDING + PADDING) - PADDING - PADDING,
							imageRenderType.height - (PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING + 17 + 17 + 17 + PADDING) - PADDING
						)

						graphics2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
						graphics2d.color = Color.WHITE

						graphics2d.font = sectionSubtitleFont


						// TODO: Somehow remove this runBlocking?
						runBlocking {
							drawAboutMeWrapSpaces(
								graphics2d,
								graphics2d.fontMetrics,
								aboutMe,
								48 + PADDING + PADDING + PADDING + PADDING,
								PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING + 17 + 17 + PADDING + 17 + 17 + 17 + PADDING + 17 + PADDING,
								(48 + PADDING + PADDING + PADDING) + w - PADDING,
								0,
								allowedDiscordEmojis
							)
						}
					}
				),
			ImageFormat.GIF
		)
	}
}