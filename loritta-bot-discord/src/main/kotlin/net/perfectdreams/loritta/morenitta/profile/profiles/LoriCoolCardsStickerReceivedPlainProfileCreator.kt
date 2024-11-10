package net.perfectdreams.loritta.morenitta.profile.profiles

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.loricoolcards.CardRarity
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.loricoolcards.LoriCoolCardsManager
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileGuildInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

open class LoriCoolCardsStickerReceivedPlainProfileCreator(
	loritta: LorittaBot,
	internalName: String,
	val rarity: CardRarity,
	val useLorittaBackground: Boolean
) : RawProfileCreator(loritta, internalName) {
	class LoriCoolCardsStickerReceivedPlainCommonProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainCommon", CardRarity.COMMON, false)
	class LoriCoolCardsStickerReceivedPlainUncommonProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainUncommon", CardRarity.UNCOMMON, false)
	class LoriCoolCardsStickerReceivedPlainRareProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainRare", CardRarity.RARE, false)
	class LoriCoolCardsStickerReceivedPlainEpicProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainEpic", CardRarity.EPIC, false)
	class LoriCoolCardsStickerReceivedPlainLegendaryProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainLegendary", CardRarity.LEGENDARY, false)
	class LoriCoolCardsStickerReceivedPlainMythicProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainMythic", CardRarity.MYTHIC, false)

	class LoriCoolCardsStickerReceivedPlainCommonUserBackgroundProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainCommonUserBackground", CardRarity.COMMON, true)
	class LoriCoolCardsStickerReceivedPlainUncommonUserBackgroundProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainUncommonUserBackground", CardRarity.UNCOMMON, true)
	class LoriCoolCardsStickerReceivedPlainRareUserBackgroundProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainRareUserBackground", CardRarity.RARE, true)
	class LoriCoolCardsStickerReceivedPlainEpicUserBackgroundProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainEpicUserBackground", CardRarity.EPIC, true)
	class LoriCoolCardsStickerReceivedPlainLegendaryUserBackgroundProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainLegendaryUserBackground", CardRarity.LEGENDARY, true)
	class LoriCoolCardsStickerReceivedPlainMythicUserBackgroundProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainMythicUserBackground", CardRarity.MYTHIC, true)

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
		// TODO: Avatar fallback
		val avatar = ImageIO.read(URL(user.avatarUrl))

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
					LoriCoolCardsManager.StickerReceivedRenderType.ProfileDesignPlain(
						if (useLorittaBackground) {
							{ graphics2d, cardX, _ ->
								graphics2d.drawImage(
									background.getScaledInstance(
										960,
										720,
										BufferedImage.SCALE_SMOOTH
									), 0, 0, null
								)
							}
						} else {
							null
						}
					)
				),
			ImageFormat.GIF
		)
	}
}