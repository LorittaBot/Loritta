package net.perfectdreams.loritta.plugin.profiles.designs

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.profile.ProfileCreator
import com.mrpowergamerbr.loritta.profile.ProfileUserInfoData
import com.mrpowergamerbr.loritta.utils.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.profile.ProfileUtils
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream

class NextGenProfileCreator : ProfileCreator("nextGenDark") {
	override suspend fun create(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, badges: List<BufferedImage>, locale: BaseLocale, background: BufferedImage, aboutMe: String): BufferedImage {
		val profileWrapper = readImage(File(Loritta.ASSETS, "profile/next_gen/profile_wrapper.png"))

		val whitneySemiBold = FileInputStream(File(Loritta.ASSETS + "whitney-semibold.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
		val whitneyBold = FileInputStream(File(Loritta.ASSETS + "whitney-bold.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
		val whitneyMedium22 = whitneySemiBold.deriveFont(22f)
		val whitneyBold16 = whitneyBold.deriveFont(16f)

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

		val avatar = LorittaUtils.downloadImage(user.avatarUrl)!!.getScaledInstance(167, 167, BufferedImage.SCALE_SMOOTH)

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
			val marriedWith = runBlocking { lorittaShards.retrieveUserById(marriedWithId) }

			if (marriedWith != null) {
				val whitneySemiBold16 = whitneySemiBold.deriveFont(16f)
				val whitneyMedium20 = whitneyMedium22.deriveFont(20f)
				graphics.color = Color.WHITE
				graphics.font = whitneySemiBold16
				ImageUtils.drawCenteredString(graphics, locale["profile.marriedWith"], Rectangle(311, 0, 216, 14), whitneySemiBold16)
				graphics.font = whitneyMedium20
				ImageUtils.drawCenteredString(graphics, marriedWith.name + "#" + marriedWith.discriminator, Rectangle(311, 0 + 18, 216, 18), whitneyMedium20)
				graphics.font = whitneySemiBold16
				ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(marriage.marriedSince, System.currentTimeMillis(), locale), Rectangle(311, 0 + 18 + 24, 216, 14), whitneySemiBold16)
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
		graphics.drawText(user.name, 232, 143) // Nome do usuário

		graphics.color = Color.BLACK
		graphics.font = oswaldRegular42
		ImageUtils.drawCenteredString(graphics, locale["profile.aboutMe"].toUpperCase(), Rectangle(0, 427, 221, 51), oswaldRegular42)
		graphics.font = oswaldRegular36

		drawReputations(user, graphics)

		graphics.color = Color.WHITE

		drawBadges(badges, graphics)

		graphics.font = whitneyBold16
		val biggestStrWidth = drawUserInfo(user, userProfile, guild, graphics)

		graphics.color = Color.BLACK
		drawMarriageStatus(userProfile, locale, graphics)
		graphics.color = Color.WHITE

		graphics.font = whitneyMedium22

		ImageUtils.drawTextWrapSpaces(aboutMe, 8, 508, 796, 600, graphics.fontMetrics, graphics)

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
		val reputations = ProfileUtils.getReputationCount(user)

		ImageUtils.drawCenteredString(graphics, "$reputations reps", Rectangle(620, 168, 180, 55), font)
	}

	suspend fun drawUserInfo(user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, graphics: Graphics): Int {
		val userInfo = mutableListOf<String>()
		graphics.drawText("Global", 232, 157)
		userInfo.add("Global")
		val globalPosition = ProfileUtils.getGlobalExperiencePosition(userProfile)
		if (globalPosition != null)
			graphics.drawText("#$globalPosition / ${userProfile.xp} XP", 232, 173)
		else
			graphics.drawText("${userProfile.xp} XP", 232, 173)

		if (guild != null) {
			val localProfile = ProfileUtils.getLocalProfile(guild, user)

			val localPosition = ProfileUtils.getLocalExperiencePosition(localProfile)

			val xpLocal = localProfile?.xp

			// Iremos remover os emojis do nome da guild, já que ele não calcula direito no stringWidth
			graphics.drawText(guild.name.replace(Constants.EMOJI_PATTERN.toRegex(), ""), 16, 51)
			if (xpLocal != null) {
				if (localPosition != null) {
					graphics.drawText("#$localPosition / $xpLocal XP", 16, 70)
				} else {
					graphics.drawText("$xpLocal XP", 16, 70)
				}
			} else {
				graphics.drawText("???", 16, 70)
			}
		}

		graphics.color = Color.BLACK
		val globalEconomyPosition = ProfileUtils.getGlobalEconomyPosition(userProfile)

		graphics.drawText("Sonhos", 631, 34)
		if (globalEconomyPosition != null)
			graphics.drawText("#$globalEconomyPosition / ${userProfile.money}", 631, 54)
		else
			graphics.drawText("${userProfile.money}", 631, 54)

		graphics.color = Color.WHITE
		return 0
	}

	suspend fun drawMarriageStatus(userProfile: Profile, locale: BaseLocale, graphics: Graphics) {
		ProfileUtils.getMarriageInfo(userProfile)?.let { (marriage, marriedWith) ->
			graphics.drawText(locale["profile.marriedWith"], 271, 34)
			graphics.drawText("${marriedWith.name}#${marriedWith.discriminator}", 271, 54)
		}
	}
}