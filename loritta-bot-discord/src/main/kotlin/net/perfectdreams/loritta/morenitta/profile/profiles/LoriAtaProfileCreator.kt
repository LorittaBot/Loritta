package net.perfectdreams.loritta.morenitta.profile.profiles

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImageFromResources
import net.perfectdreams.loritta.common.utils.LorittaImage
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileGuildInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUtils
import java.awt.Color
import java.awt.Rectangle
import java.awt.image.BufferedImage

class LoriAtaProfileCreator(loritta: LorittaBot) : StaticProfileCreator(loritta, "loriAta") {
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
	): BufferedImage {
		val profileWrapper = readImageFromResources("/profile/lori_ata/profile_wrapper.png")

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

		val userInfo = mutableListOf<String>()
		userInfo.add("Global")
		userInfo.add("${userProfile.xp} XP")

		if (guild != null) {
			val localProfile = ProfileUtils.getLocalProfile(loritta, guild, user)

			val localPosition = ProfileUtils.getLocalExperiencePosition(loritta, localProfile)

			val xpLocal = localProfile?.xp

			// Iremos remover os emojis do nome da guild, já que ele não calcula direito no stringWidth
			userInfo.add(guild.name.replace(Constants.EMOJI_PATTERN.toRegex(), ""))
			if (xpLocal != null) {
				if (localPosition != null) {
					userInfo.add("#$localPosition / $xpLocal XP")
				} else {
					userInfo.add("$xpLocal XP")
				}
			} else {
				userInfo.add("???")
			}
		}

		val globalEconomyPosition = ProfileUtils.getGlobalEconomyPosition(loritta, userProfile)

		userInfo.add("Sonhos")
		if (globalEconomyPosition != null)
			userInfo.add("#$globalEconomyPosition / ${userProfile.money}")
		else
			userInfo.add("${userProfile.money}")

		graphics.font = loritta.graphicsFonts.komikaHand.deriveFont(13f)
		val biggestStrWidth = graphics.fontMetrics.stringWidth(userInfo.maxByOrNull { graphics.fontMetrics.stringWidth(it) }!!)

		val avatar = LorittaUtils.downloadImage(loritta, user.avatarUrl)!!.getScaledInstance(148, 148, BufferedImage.SCALE_SMOOTH)

		val image = LorittaImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH).toBufferedImage())

		image.setCorners(
				320f, 0f,
				800f, 0f,
				800f, 346f,
				330f, 250f
		)

		graphics.drawImage(image.bufferedImage, 0, 0, null)
		graphics.drawImage(profileWrapper, 0, 0, null)
		graphics.drawImage(avatar.toBufferedImage().makeRoundedCorners(148), 6, 446, null)

		graphics.font = loritta.graphicsFonts.komikaHand.deriveFont(27f)
		graphics.color = Color.BLACK
		graphics.drawText(loritta, user.name, 161, 509, 527)
		graphics.font = loritta.graphicsFonts.komikaHand.deriveFont(16f)
		graphics.drawStringWrap(loritta, aboutMe, 161, 532, 773 - biggestStrWidth - 4)

		val reputations = ProfileUtils.getReputationCount(loritta, user)

		graphics.font = loritta.graphicsFonts.komikaHand.deriveFont(32f)

		ImageUtils.drawCenteredString(graphics, "${reputations} reps", Rectangle(552, 440, 228, 54), graphics.font)

		if (badges.isNotEmpty()) {
			val badgesBackground = readImageFromResources("/profile/monica_ata/badges.png")
			graphics.drawImage(badgesBackground, 0, 0, null)

			var x = 196
			for (badge in badges) {
				graphics.drawImage(badge.getScaledInstance(27, 27, BufferedImage.SCALE_SMOOTH), x, 447, null)
				x += 29
			}
		}

		ProfileUtils.getMarriageInfo(loritta, userProfile)?.let { (marriage, marriedWith) ->
			val marrySection = readImageFromResources("/profile/monica_ata/marry.png")
			graphics.drawImage(marrySection, 200, 0, null)

			graphics.font = loritta.graphicsFonts.komikaHand.deriveFont(21f)
			ImageUtils.drawCenteredString(graphics, locale["profile.marriedWith"], Rectangle(200 + 280, 270, 218, 22), graphics.font)
			graphics.font = loritta.graphicsFonts.komikaHand.deriveFont(16f)
			ImageUtils.drawCenteredString(graphics, marriedWith.name, Rectangle(200 + 280, 270 + 23, 218, 18), graphics.font)
			graphics.font = loritta.graphicsFonts.komikaHand.deriveFont(12f)
			ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(i18nContext, marriage.marriedSince, System.currentTimeMillis(), 3), Rectangle(200 + 280, 270 + 23 + 16, 218, 15), graphics.font)
		}

		graphics.font = loritta.graphicsFonts.komikaHand.deriveFont(13f)
		var y = 513
		for (line in userInfo) {
			graphics.drawText(loritta, line, 773 - biggestStrWidth - 2, y)
			y += 14
		}

		return base.makeRoundedCorners(15)
	}
}