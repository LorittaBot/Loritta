package net.perfectdreams.loritta.morenitta.profile.profiles

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImageFromResources
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileGuildInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUtils
import java.awt.*
import java.awt.image.BufferedImage

class NextGenProfileCreator(loritta: LorittaBot) : StaticProfileCreator(loritta, "nextGenDark") {
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
		val profileWrapper = readImageFromResources("/profile/next_gen/profile_wrapper.png")

		val latoBold = loritta.graphicsFonts.latoBold
		val latoBlack = loritta.graphicsFonts.latoBlack
		val latoRegular22 = latoBold.deriveFont(22f)
		val latoBlack16 = latoBlack.deriveFont(16f)

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

		val avatar = LorittaUtils.downloadImage(loritta, user.avatarUrl)!!.getScaledInstance(167, 167, BufferedImage.SCALE_SMOOTH)

		graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)

		drawAvatar(avatar, graphics)

		val marriage = loritta.newSuspendedTransaction { userProfile.marriage }

		/* if (marriage != null) {
			val marriedWithId = if (marriage.user1 == user.id) {
				marriage.user2
			} else {
				marriage.user1
			}.toString()

			val marrySection = ImageIO.read(File(Loritta.ASSETS, "profile/cowboy/marry.png"))
			graphics.drawImage(marrySection, 0, 0, null)
			val marriedWith = runBlocking { loritta.lorittaShards.retrieveUserById(marriedWithId) }

			if (marriedWith != null) {
				val latoBold16 = latoBold.deriveFont(16f)
				val latoRegular20 = latoRegular22.deriveFont(20f)
				graphics.color = Color.WHITE
				graphics.font = latoBold16
				ImageUtils.drawCenteredString(graphics, locale["profile.marriedWith"], Rectangle(311, 0, 216, 14), latoBold16)
				graphics.font = latoRegular20
				ImageUtils.drawCenteredString(graphics, marriedWith.name + "#" + marriedWith.discriminator, Rectangle(311, 0 + 18, 216, 18), latoRegular20)
				graphics.font = latoBold16
				ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(marriage.marriedSince, System.currentTimeMillis(), locale), Rectangle(311, 0 + 18 + 24, 216, 14), latoBold16)
			}
		} */

		graphics.color = Color.WHITE
		graphics.drawImage(profileWrapper, 0, 0, null)

		val oswaldRegular36 = Constants.OSWALD_REGULAR
				.deriveFont(36f)
		val oswaldRegular50 = Constants.OSWALD_REGULAR
				.deriveFont(50F)
		val oswaldRegular42 = Constants.OSWALD_REGULAR
				.deriveFont(42F)

		graphics.font = oswaldRegular36
		graphics.drawText(loritta, user.name, 232, 143) // Nome do usuário

		graphics.color = Color.BLACK
		graphics.font = oswaldRegular42
		ImageUtils.drawCenteredString(graphics, locale["profile.aboutMe"].toUpperCase(), Rectangle(0, 427, 221, 51), oswaldRegular42)
		graphics.font = oswaldRegular36

		drawReputations(user, graphics)

		graphics.color = Color.WHITE

		drawBadges(badges, graphics)

		graphics.font = latoBlack16
		val biggestStrWidth = drawUserInfo(user, userProfile, guild, graphics)

		graphics.color = Color.BLACK
		drawMarriageStatus(userProfile, locale, graphics)
		graphics.color = Color.WHITE

		graphics.font = latoRegular22

		drawAboutMeWrapSpaces(graphics, graphics.fontMetrics, aboutMe, 8, 508, 796, 600, allowedDiscordEmojis)

		return base.makeRoundedCorners(15)
	}

	fun drawAvatar(avatar: Image, graphics: Graphics) {
		graphics.drawImage(
				avatar
						.toBufferedImage()
						.makeRoundedCorners(999),
				46,
				95,
				null
		)
	}

	fun drawBadges(badges: List<BufferedImage>, graphics: Graphics) {
		var x = 243
		for (badge in badges) {
			graphics.drawImage(badge.getScaledInstance(24, 24, BufferedImage.SCALE_SMOOTH), x, 428, null)
			x += 26
		}
	}

	suspend fun drawReputations(user: ProfileUserInfoData, graphics: Graphics) {
		val font = graphics.font
		val reputations = ProfileUtils.getReputationCount(loritta, user)

		ImageUtils.drawCenteredString(graphics, "$reputations reps", Rectangle(620, 168, 180, 55), font)
	}

	suspend fun drawUserInfo(user: ProfileUserInfoData, userProfile: Profile, guild: ProfileGuildInfoData?, graphics: Graphics): Int {
		val userInfo = mutableListOf<String>()
		graphics.drawText(loritta, "Global", 232, 157)
		userInfo.add("Global")
		graphics.drawText(loritta, "${userProfile.xp} XP", 232, 173)

		if (guild != null) {
			val localProfile = ProfileUtils.getLocalProfile(loritta, guild, user)

			val localPosition = ProfileUtils.getLocalExperiencePosition(loritta, localProfile)

			val xpLocal = localProfile?.xp

			// Iremos remover os emojis do nome da guild, já que ele não calcula direito no stringWidth
			graphics.drawText(loritta, guild.name.replace(Constants.EMOJI_PATTERN.toRegex(), ""), 16, 51)
			if (xpLocal != null) {
				if (localPosition != null) {
					graphics.drawText(loritta, "#$localPosition / $xpLocal XP", 16, 70)
				} else {
					graphics.drawText(loritta, "$xpLocal XP", 16, 70)
				}
			} else {
				graphics.drawText(loritta, "???", 16, 70)
			}
		}

		graphics.color = Color.BLACK
		val globalEconomyPosition = ProfileUtils.getGlobalEconomyPosition(loritta, userProfile)

		graphics.drawText(loritta, "Sonhos", 631, 34)
		if (globalEconomyPosition != null)
			graphics.drawText(loritta, "#$globalEconomyPosition / ${userProfile.money}", 631, 54)
		else
			graphics.drawText(loritta, "${userProfile.money}", 631, 54)

		graphics.color = Color.WHITE
		return 0
	}

	suspend fun drawMarriageStatus(userProfile: Profile, locale: BaseLocale, graphics: Graphics) {
		ProfileUtils.getMarriageInfo(loritta, userProfile)?.let { (marriage, marriedWith) ->
			graphics.drawText(loritta, locale["profile.marriedWith"], 271, 34)
			graphics.drawText(loritta, "${marriedWith.name}#${marriedWith.discriminator}", 271, 54)
		}
	}
}