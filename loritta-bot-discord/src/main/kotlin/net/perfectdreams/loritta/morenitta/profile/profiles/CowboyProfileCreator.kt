package net.perfectdreams.loritta.morenitta.profile.profiles

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImageFromResources
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileGuildInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUtils
import net.perfectdreams.loritta.morenitta.utils.*
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import java.awt.Rectangle
import java.awt.image.BufferedImage

class CowboyProfileCreator(loritta: LorittaBot) : StaticProfileCreator(loritta, "cowboy") {
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
		val profileWrapper = readImageFromResources("/profile/cowboy/profile_wrapper.png")

		val latoBold = loritta.graphicsFonts.latoBold
		val latoBlack = loritta.graphicsFonts.latoBlack
		val latoRegular22 = latoBold.deriveFont(22f)
		val latoBlack16 = latoBlack.deriveFont(16f)

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

		val avatar = LorittaUtils.downloadImage(loritta, user.avatarUrl)!!.getScaledInstance(147, 147, BufferedImage.SCALE_SMOOTH)

		graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)

		drawAvatar(avatar, graphics)

		ProfileUtils.getMarriageInfo(loritta, userProfile)?.let { (marriage, marriedWith) ->
			val marrySection = readImageFromResources("/profile/cowboy/marry.png")
			graphics.drawImage(marrySection, 0, 0, null)

			val latoBold16 = latoBold.deriveFont(16f)
			val latoRegular20 = latoRegular22.deriveFont(20f)
			graphics.color = Color.WHITE
			graphics.font = latoBold16
			ImageUtils.drawCenteredString(graphics, locale["profile.marriedWith"], Rectangle(311, 0, 216, 14), latoBold16)
			graphics.font = latoRegular20
			ImageUtils.drawCenteredString(graphics, marriedWith.name, Rectangle(311, 0 + 18, 216, 18), latoRegular20)
			graphics.font = latoBold16
			ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(i18nContext, marriage.marriedSince, System.currentTimeMillis(), 3), Rectangle(311, 0 + 18 + 24, 216, 14), latoBold16)
		}

		graphics.color = Color.BLACK
		graphics.drawImage(profileWrapper, 0, 0, null)

		val oswaldRegular50 = Constants.OSWALD_REGULAR
				.deriveFont(50F)
		val oswaldRegular42 = Constants.OSWALD_REGULAR
				.deriveFont(42F)

		graphics.font = oswaldRegular50
		graphics.drawText(loritta, user.name, 162, 506) // Nome do usuário
		graphics.font = oswaldRegular42

		drawReputations(user, graphics)

		drawBadges(badges, graphics)

		graphics.font = latoBlack16
		val biggestStrWidth = drawUserInfo(user, userProfile, guild, graphics)

		graphics.font = latoRegular22

		drawAboutMeWrapSpaces(graphics, graphics.fontMetrics, aboutMe, 162, 529, 773 - biggestStrWidth - 4, 600, allowedDiscordEmojis)

		return base.makeRoundedCorners(15)
	}

	fun drawAvatar(avatar: Image, graphics: Graphics) {
		graphics.drawImage(
				avatar.toBufferedImage()
						.getSubimage(0, 19, 147, 147 - 19),
				6,
				466,
				null
		)
	}

	fun drawBadges(badges: List<BufferedImage>, graphics: Graphics) {
		var x = 191
		for (badge in badges) {
			graphics.drawImage(badge.getScaledInstance(33, 33, BufferedImage.SCALE_SMOOTH), x, 427, null)
			x += 35
		}
	}

	suspend fun drawReputations(user: ProfileUserInfoData, graphics: Graphics) {
		val font = graphics.font
		val reputations = ProfileUtils.getReputationCount(loritta, user)
		ImageUtils.drawCenteredString(graphics, "$reputations reps", Rectangle(582, 0, 218, 66), font)
	}

	suspend fun drawUserInfo(user: ProfileUserInfoData, userProfile: Profile, guild: ProfileGuildInfoData?, graphics: Graphics): Int {
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

		val biggestStrWidth = graphics.fontMetrics.stringWidth(userInfo.maxByOrNull { graphics.fontMetrics.stringWidth(it) }!!)

		var y = 480
		for (line in userInfo) {
			graphics.drawText(loritta, line, 773 - biggestStrWidth - 2, y)
			y += 18
		}

		return biggestStrWidth
	}
}