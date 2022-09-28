package net.perfectdreams.loritta.morenitta.profile.profiles

import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.profile.ProfileGuildInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream

open class PlainProfileCreator(loritta: LorittaBot, internalName: String, val folderName: String) : StaticProfileCreator(loritta, internalName) {
	class PlainWhiteProfileCreator(loritta: LorittaBot) : PlainProfileCreator(loritta, "plainWhite", "white")
	class PlainOrangeProfileCreator(loritta: LorittaBot) : PlainProfileCreator(loritta, "plainOrange", "orange")
	class PlainPurpleProfileCreator(loritta: LorittaBot) : PlainProfileCreator(loritta, "plainPurple", "purple")
	class PlainAquaProfileCreator(loritta: LorittaBot) : PlainProfileCreator(loritta, "plainAqua", "aqua")
	class PlainGreenProfileCreator(loritta: LorittaBot) : PlainProfileCreator(loritta, "plainGreen", "green")
	class PlainGreenHeartsProfileCreator(loritta: LorittaBot) : PlainProfileCreator(loritta, "plainGreenHearts", "green_hearts")

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
        allowedDiscordEmojis: List<Snowflake>?
	): BufferedImage {
		val profileWrapper = readImage(File(LorittaBot.ASSETS, "profile/plain/profile_wrapper_$folderName.png"))

		val whitneySemiBold = FileInputStream(File(LorittaBot.ASSETS + "whitney-semibold.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
		val whitneyBold = FileInputStream(File(LorittaBot.ASSETS + "whitney-bold.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
		val whitneyMedium22 = whitneySemiBold.deriveFont(22f)
		val whitneyBold16 = whitneyBold.deriveFont(16f)
		val whitneyMedium16 = whitneySemiBold.deriveFont(16f)
		val whitneyBold12 = whitneyBold.deriveFont(12f)

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

		val avatar = LorittaUtils.downloadImage(loritta, user.avatarUrl)!!.getScaledInstance(152, 152, BufferedImage.SCALE_SMOOTH)

		graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)

		ProfileUtils.getMarriageInfo(loritta, userProfile)?.let { (marriage, marriedWith) ->
			val marrySection = readImage(File(LorittaBot.ASSETS, "profile/plain/marry.png"))
			graphics.drawImage(marrySection, 0, 0, null)

			graphics.color = Color.WHITE
			graphics.font = whitneyBold12
			ImageUtils.drawCenteredString(graphics, locale["profile.marriedWith"], Rectangle(635, 350, 165, 14), whitneyBold12)
			graphics.font = whitneyMedium16
			ImageUtils.drawCenteredString(graphics, marriedWith.name + "#" + marriedWith.discriminator, Rectangle(635, 350 + 16, 165, 18), whitneyMedium16)
			graphics.font = whitneyBold12
			ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(marriage.marriedSince, System.currentTimeMillis(), locale), Rectangle(635, 350 + 16 + 18, 165, 14), whitneyBold12)
		}

		graphics.color = Color.BLACK
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

		graphics.font = whitneyBold16
		val biggestStrWidth = drawUserInfo(user, userProfile, guild, graphics)

		graphics.font = whitneyMedium22

		ImageUtils.drawTextWrapSpaces(loritta, aboutMe, 162, 484, 773 - biggestStrWidth - 4, 600, graphics.fontMetrics, graphics)

		return base.makeRoundedCorners(15)
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
		val globalPosition = ProfileUtils.getGlobalExperiencePosition(loritta, userProfile)
		if (globalPosition != null)
			userInfo.add("#$globalPosition / ${userProfile.xp} XP")
		else
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