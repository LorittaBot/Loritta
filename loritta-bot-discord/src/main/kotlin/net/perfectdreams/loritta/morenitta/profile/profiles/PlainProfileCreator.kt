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

open class PlainProfileCreator(loritta: LorittaBot, internalName: String, val folderName: String, val textColor: Color) : StaticProfileCreator(loritta, internalName) {
	class PlainWhiteProfileCreator(loritta: LorittaBot) : PlainProfileCreator(loritta, "plainWhite", "white", Color.BLACK)
	class PlainOrangeProfileCreator(loritta: LorittaBot) : PlainProfileCreator(loritta, "plainOrange", "orange", Color.BLACK)
	class PlainPurpleProfileCreator(loritta: LorittaBot) : PlainProfileCreator(loritta, "plainPurple", "purple", Color.BLACK)
	class PlainAquaProfileCreator(loritta: LorittaBot) : PlainProfileCreator(loritta, "plainAqua", "aqua", Color.BLACK)
	class PlainGreenProfileCreator(loritta: LorittaBot) : PlainProfileCreator(loritta, "plainGreen", "green", Color.BLACK)
	class PlainGreenHeartsProfileCreator(loritta: LorittaBot) : PlainProfileCreator(loritta, "plainGreenHearts", "green_hearts", Color.BLACK)
	class PlainPureBlackProfileCreator(loritta: LorittaBot) : PlainProfileCreator(loritta, "plainPureBlack", "pure_black", Color.WHITE)

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
		val profileWrapper = readImageFromResources("/profile/plain/profile_wrapper_$folderName.png")

		val latoBold = loritta.graphicsFonts.latoBold
		val latoBlack = loritta.graphicsFonts.latoBlack
		val latoRegular22 = latoBold.deriveFont(22f)
		val latoBlack16 = latoBlack.deriveFont(16f)
		val latoRegular16 = latoBold.deriveFont(16f)
		val latoBlack12 = latoBlack.deriveFont(12f)

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

		val avatar = (LorittaUtils.downloadImage(loritta, user.avatarUrl) ?: Constants.DEFAULT_DISCORD_BLUE_AVATAR).getScaledInstance(152, 152, BufferedImage.SCALE_SMOOTH)

		graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)

		ProfileUtils.getMarriageInfo(loritta, userProfile)?.let { (marriage, marriedWith) ->
			val marrySection = readImageFromResources("/profile/plain/marry.png")
			graphics.drawImage(marrySection, 0, 0, null)

			graphics.color = Color.WHITE
			graphics.font = latoBlack12
			ImageUtils.drawCenteredString(graphics, locale["profile.marriedWith"], Rectangle(635, 350, 165, 14), latoBlack12)
			graphics.font = latoRegular16
			ImageUtils.drawCenteredString(graphics, marriedWith.name, Rectangle(635, 350 + 16, 165, 18), latoRegular16)
			graphics.font = latoBlack12
			ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(i18nContext, marriage.marriedSince.toEpochMilliseconds(), System.currentTimeMillis(), 3), Rectangle(635, 350 + 16 + 18, 165, 14), latoBlack12)
		}

		graphics.color = textColor
		graphics.drawImage(profileWrapper, 0, 0, null)
		drawAvatar(avatar, graphics)

		val oswaldRegular50 = Constants.OSWALD_REGULAR
			.deriveFont(50F)
		val oswaldRegular42 = Constants.OSWALD_REGULAR
			.deriveFont(42F)

		graphics.font = oswaldRegular50
		graphics.drawText(loritta, user.name, 162, 461) // Nome do usuário
		graphics.font = oswaldRegular42

		drawReputations(user, graphics)

		drawBadges(badges, graphics)

		graphics.font = latoBlack16
		val biggestStrWidth = drawUserInfo(user, userProfile, guild, graphics)

		graphics.font = latoRegular22

		drawAboutMeWrapSpaces(graphics, graphics.fontMetrics, aboutMe, 162, 484, 773 - biggestStrWidth - 4, 600, allowedDiscordEmojis)

		return base
	}

	fun drawAvatar(avatar: Image, graphics: Graphics) {
		graphics.drawImage(
			avatar.toBufferedImage()
				.makeRoundedCorners(999),
			3,
			406,
			null
		)
	}

	fun drawBadges(badges: List<BufferedImage>, graphics: Graphics) {
		var x = 2
		for (badge in badges) {
			graphics.drawImage(badge.getScaledInstance(35, 35, BufferedImage.SCALE_SMOOTH), x, 564, null)
			x += 37
		}
	}

	suspend fun drawReputations(user: ProfileUserInfoData, graphics: Graphics) {
		val font = graphics.font
		val reputations = ProfileUtils.getReputationCount(loritta, user)

		ImageUtils.drawCenteredString(graphics, "$reputations reps", Rectangle(634, 404, 166, 52), font)
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

		var y = 475
		for (line in userInfo) {
			graphics.drawText(loritta, line, 773 - biggestStrWidth - 2, y)
			y += 16
		}

		return biggestStrWidth
	}
}