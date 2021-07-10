package net.perfectdreams.loritta.plugin.profiles.designs

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.profile.ProfileCreator
import com.mrpowergamerbr.loritta.profile.ProfileUserInfoData
import com.mrpowergamerbr.loritta.utils.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.profile.ProfileUtils
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream

class Halloween2019ProfileCreator : ProfileCreator("halloween2019") {
	override suspend fun create(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, badges: List<BufferedImage>, locale: BaseLocale, background: BufferedImage, aboutMe: String): BufferedImage {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override suspend fun createGif(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, badges: List<BufferedImage>, locale: BaseLocale, background: BufferedImage, aboutMe: String): List<BufferedImage> {
		val list = mutableListOf<BufferedImage>()

		val whitneySemiBold = FileInputStream(File(Loritta.ASSETS + "whitney-semibold.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
		val whitneyBold = FileInputStream(File(Loritta.ASSETS + "whitney-bold.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}

		val whitneyMedium22 = whitneySemiBold.deriveFont(22f)
		val whitneyBold16 = whitneyBold.deriveFont(16f)
		val whitneyMedium16 = whitneySemiBold.deriveFont(16f)
		val whitneyBold12 = whitneyBold.deriveFont(12f)
		val oswaldRegular50 = Constants.OSWALD_REGULAR
				.deriveFont(50F)
		val oswaldRegular42 = Constants.OSWALD_REGULAR
				.deriveFont(42F)

		val avatar = LorittaUtils.downloadImage(user.avatarUrl)!!.getScaledInstance(152, 152, BufferedImage.SCALE_SMOOTH)
		val marrySection = readImage(File(Loritta.ASSETS, "profile/halloween_2019/marry.png"))

		val marriage = loritta.newSuspendedTransaction { userProfile.marriage }

		val marriedWithId = if (marriage?.user1 == user.id) {
			marriage.user2
		} else {
			marriage?.user1
		}

		val marriedWith = if (marriedWithId != null) { lorittaShards.retrieveUserInfoById(marriedWithId.toLong()) } else { null }

		val reputations = ProfileUtils.getReputationCount(user)

		val globalPosition = ProfileUtils.getGlobalExperiencePosition(userProfile)

		var xpLocal: Long? = null
		var localPosition: Long? = null

		if (guild != null) {
			val localProfile = ProfileUtils.getLocalProfile(guild, user)

			localPosition = ProfileUtils.getLocalExperiencePosition(localProfile)
			xpLocal = localProfile?.xp
		}

		val globalEconomyPosition = ProfileUtils.getGlobalEconomyPosition(userProfile)

		val resizedBadges = badges.map { it.getScaledInstance(35, 35, BufferedImage.SCALE_SMOOTH).toBufferedImage() }

		for (i in 0..29) {
			val profileWrapper = readImage(File(Loritta.ASSETS, "profile/halloween_2019/frames/halloween_2019_${i.toString().padStart(6, '0')}.png"))

			val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
			val graphics = base.graphics.enableFontAntiAliasing()

			graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)

			graphics.color = Color.BLACK
			graphics.drawImage(profileWrapper, 0, 0, null)
			drawAvatar(avatar, graphics)

			graphics.font = oswaldRegular50
			graphics.drawText(user.name, 162, 461) // Nome do usuário
			graphics.font = oswaldRegular42

			drawReputations(user, graphics, reputations)

			drawBadges(resizedBadges, graphics)

			graphics.font = whitneyBold16
			val biggestStrWidth = drawUserInfo(user, userProfile, guild, graphics, globalPosition, localPosition, xpLocal, globalEconomyPosition)

			graphics.font = whitneyMedium22

			ImageUtils.drawTextWrapSpaces(aboutMe, 162, 484, 773 - biggestStrWidth - 4, 600, graphics.fontMetrics, graphics)

			if (marriage != null) {
				graphics.drawImage(marrySection, 0, 0, null)

				if (marriedWith != null) {
					graphics.color = Color.WHITE
					graphics.font = whitneyBold12
					ImageUtils.drawCenteredString(graphics, locale["profile.marriedWith"], Rectangle(635, 350, 165, 14), whitneyBold12)
					graphics.font = whitneyMedium16
					ImageUtils.drawCenteredString(graphics, marriedWith.name + "#" + marriedWith.discriminator, Rectangle(635, 350 + 16, 165, 18), whitneyMedium16)
					graphics.font = whitneyBold12
					ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(marriage.marriedSince, System.currentTimeMillis(), locale), Rectangle(635, 350 + 16 + 18, 165, 14), whitneyBold12)
				}
			}

			list.add(base.getScaledInstance(400, 300, BufferedImage.SCALE_SMOOTH).toBufferedImage())
		}

		return list
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
			graphics.drawImage(badge, x, 564, null)
			x += 37
		}
	}

	fun drawReputations(user: ProfileUserInfoData, graphics: Graphics, reputations: Long) {
		val font = graphics.font

		ImageUtils.drawCenteredString(graphics, "$reputations reps", Rectangle(634, 404, 166, 52), font)
	}

	fun drawUserInfo(user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, graphics: Graphics, globalPosition: Long?, localPosition: Long?, xpLocal: Long?, globalEconomyPosition: Long?): Int {
		val userInfo = mutableListOf<String>()
		userInfo.add("Global")

		if (globalPosition != null)
			userInfo.add("#$globalPosition / ${userProfile.xp} XP")
		else
			userInfo.add("${userProfile.xp} XP")

		if (guild != null) {
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

		userInfo.add("Sonhos")
		if (globalEconomyPosition != null)
			userInfo.add("#$globalEconomyPosition / ${userProfile.money}")
		else
			userInfo.add("${userProfile.money}")

		val biggestStrWidth = graphics.fontMetrics.stringWidth(userInfo.maxByOrNull { graphics.fontMetrics.stringWidth(it) }!!)

		var y = 475
		for (line in userInfo) {
			graphics.drawText(line, 773 - biggestStrWidth - 2, y)
			y += 16
		}

		return biggestStrWidth
	}
}