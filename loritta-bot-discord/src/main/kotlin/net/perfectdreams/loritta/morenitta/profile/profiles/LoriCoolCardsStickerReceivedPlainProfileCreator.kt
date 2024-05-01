package net.perfectdreams.loritta.morenitta.profile.profiles

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.loricoolcards.CardRarity
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.loricoolcards.LoriCoolCardsManager
import net.perfectdreams.loritta.morenitta.profile.ProfileGuildInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

open class LoriCoolCardsStickerReceivedPlainProfileCreator(loritta: LorittaBot, internalName: String, val rarity: CardRarity) : RawProfileCreator(loritta, internalName) {
	class LoriCoolCardsStickerReceivedPlainCommonProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainCommon", CardRarity.COMMON)
	class LoriCoolCardsStickerReceivedPlainUncommonProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainUncommon", CardRarity.UNCOMMON)
	class LoriCoolCardsStickerReceivedPlainRareProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainRare", CardRarity.RARE)
	class LoriCoolCardsStickerReceivedPlainEpicProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainEpic", CardRarity.EPIC)
	class LoriCoolCardsStickerReceivedPlainLegendaryProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainLegendary", CardRarity.LEGENDARY)
	class LoriCoolCardsStickerReceivedPlainMythicProfileCreator(loritta: LorittaBot) : LoriCoolCardsStickerReceivedPlainProfileCreator(loritta, "loriCoolCardsStickerReceivedPlainMythic", CardRarity.MYTHIC)

	override suspend fun create(
        sender: ProfileUserInfoData,
        user: ProfileUserInfoData,
        userProfile: Profile,
        guild: ProfileGuildInfoData?,
        badges: List<BufferedImage>,
        locale: BaseLocale,
        i18nContext: I18nContext,
        background: BufferedImage,
        aboutMe: String,
        allowedDiscordEmojis: List<Long>?
	): Pair<ByteArray, ImageFormat> {
		// TODO: The ProfileDesignManager should provide the badges data list to us instead of querying it here
		val badgesData = loritta.profileDesignManager.getUserBadges(
			user,
			userProfile,
			setOf(), // We don't need this
			failIfClusterIsOffline = false // We also don't need this
		)

		// TODO: Avatar fallback
		val avatar = ImageIO.read(URL(user.avatarUrl))
		val activeBadgeUniqueId = loritta.transaction { userProfile.settings.activeBadge }
		val equippedBadge = badgesData.firstOrNull { it.id == activeBadgeUniqueId }

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
					LoriCoolCardsManager.StickerReceivedRenderType.ProfileDesignPlain
				),
			ImageFormat.GIF
		)
	}
}