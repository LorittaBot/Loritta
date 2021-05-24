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

class CowboyProfileCreator : ProfileCreator("cowboy") {
	override suspend fun create(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, badges: List<BufferedImage>, locale: BaseLocale, background: BufferedImage, aboutMe: String): BufferedImage {
		val profileWrapper = readImage(File(Loritta.ASSETS, "profile/cowboy/profile_wrapper.png"))

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

		val avatar = LorittaUtils.downloadImage(user.avatarUrl)!!.getScaledInstance(147, 147, BufferedImage.SCALE_SMOOTH)

		graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)

		drawAvatar(avatar, graphics)

		ProfileUtils.getMarriageInfo(userProfile)?.let { (marriage, marriedWith) ->
			val marrySection = readImage(File(Loritta.ASSETS, "profile/cowboy/marry.png"))
			graphics.drawImage(marrySection, 0, 0, null)

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

		graphics.color = Color.BLACK
		graphics.drawImage(profileWrapper, 0, 0, null)

		val oswaldRegular50 = Constants.OSWALD_REGULAR
				.deriveFont(50F)
		val oswaldRegular42 = Constants.OSWALD_REGULAR
				.deriveFont(42F)

		graphics.font = oswaldRegular50
		graphics.drawText(user.name, 162, 506) // Nome do usuário
		graphics.font = oswaldRegular42

		drawReputations(user, graphics)

		drawBadges(badges, graphics)

		graphics.font = whitneyBold16
		val biggestStrWidth = drawUserInfo(user, userProfile, guild, graphics)

		graphics.font = whitneyMedium22

		ImageUtils.drawTextWrapSpaces(aboutMe, 162, 529, 773 - biggestStrWidth - 4, 600, graphics.fontMetrics, graphics)

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
		val reputations = ProfileUtils.getReputationCount(user)
		ImageUtils.drawCenteredString(graphics, "$reputations reps", Rectangle(582, 0, 218, 66), font)
	}

	suspend fun drawUserInfo(user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, graphics: Graphics): Int {
		val userInfo = mutableListOf<String>()
		userInfo.add("Global")

		val globalPosition = ProfileUtils.getGlobalExperiencePosition(userProfile)
		if (globalPosition != null)
			userInfo.add("#$globalPosition / ${userProfile.xp} XP")
		else
			userInfo.add("${userProfile.xp} XP")

		if (guild != null) {
			val localProfile = ProfileUtils.getLocalProfile(guild, user)

			val localPosition = ProfileUtils.getLocalExperiencePosition(localProfile)

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

		val globalEconomyPosition = ProfileUtils.getGlobalEconomyPosition(userProfile)

		userInfo.add("Sonhos")
		if (globalEconomyPosition != null)
			userInfo.add("#$globalEconomyPosition / ${userProfile.money}")
		else
			userInfo.add("${userProfile.money}")

		val biggestStrWidth = graphics.fontMetrics.stringWidth(userInfo.maxByOrNull { graphics.fontMetrics.stringWidth(it) }!!)

		var y = 480
		for (line in userInfo) {
			graphics.drawText(line, 773 - biggestStrWidth - 2, y)
			y += 18
		}

		return biggestStrWidth
	}
}