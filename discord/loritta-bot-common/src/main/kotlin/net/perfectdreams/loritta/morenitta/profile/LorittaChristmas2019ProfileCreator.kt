package net.perfectdreams.loritta.morenitta.profile

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream

class LorittaChristmas2019ProfileCreator(val loritta: LorittaBot) : ProfileCreator("lorittaChristmas2019") {
	override suspend fun create(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, badges: List<BufferedImage>, locale: BaseLocale, background: BufferedImage, aboutMe: String): BufferedImage {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override suspend fun createGif(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, badges: List<BufferedImage>, locale: BaseLocale, background: BufferedImage, aboutMe: String): List<BufferedImage> {
		val list = mutableListOf<BufferedImage>()

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
		val oswaldRegular50 = Constants.OSWALD_REGULAR
				.deriveFont(50F)
		val oswaldRegular42 = Constants.OSWALD_REGULAR
				.deriveFont(42F)

		val avatar = LorittaUtils.downloadImage(loritta, user.avatarUrl)!!.getScaledInstance(150, 150, BufferedImage.SCALE_SMOOTH)
		val marrySection = readImage(File(LorittaBot.ASSETS, "profile/christmas_2019/marry.png"))

		val marriage = loritta.newSuspendedTransaction { userProfile.marriage }

		val marriedWithId = if (marriage?.user1 == user.id) {
			marriage.user2
		} else {
			marriage?.user1
		}

		val marriedWith = if (marriedWithId != null) { loritta.lorittaShards.retrieveUserInfoById(marriedWithId.toLong()) } else { null }

		val reputations = ProfileUtils.getReputationCount(loritta, user)

		val globalPosition = ProfileUtils.getGlobalExperiencePosition(loritta, userProfile)

		var xpLocal: Long? = null
		var localPosition: Long? = null

		if (guild != null) {
			val localProfile = ProfileUtils.getLocalProfile(loritta, guild, user)

			localPosition = ProfileUtils.getLocalExperiencePosition(loritta, localProfile)
			xpLocal = localProfile?.xp
		}

		val globalEconomyPosition = ProfileUtils.getGlobalEconomyPosition(loritta, userProfile)

		val resizedBadges = badges.map { it.getScaledInstance(30, 30, BufferedImage.SCALE_SMOOTH).toBufferedImage() }

		for (i in 0..7) {
			val profileWrapper = readImage(File(LorittaBot.ASSETS, "profile/christmas_2019/frames/christmas_2019_${i.toString().padStart(6, '0')}.png"))

			val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
			val graphics = base.graphics.enableFontAntiAliasing()

			graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)

			graphics.color = Color.BLACK
			graphics.drawImage(profileWrapper, 0, 0, null)
			drawAvatar(avatar, graphics)

			graphics.font = oswaldRegular50
			graphics.drawText(loritta, user.name, 162, 480) // Nome do usuário
			graphics.font = oswaldRegular42

			drawReputations(user, graphics, reputations)

			drawBadges(resizedBadges, graphics)

			graphics.font = whitneyBold16
			val biggestStrWidth = drawUserInfo(user, userProfile, guild, graphics, globalPosition, localPosition, xpLocal, globalEconomyPosition)

			graphics.font = whitneyMedium22

			ImageUtils.drawTextWrapSpaces(loritta, aboutMe, 162, 504, 773 - biggestStrWidth - 4, 600, graphics.fontMetrics, graphics)

			if (marriage != null) {
				graphics.drawImage(marrySection, 0, 0, null)

				if (marriedWith != null) {
					graphics.color = Color.WHITE
					graphics.font = whitneyBold12
					ImageUtils.drawCenteredString(graphics, locale["profile.marriedWith"], Rectangle(635, 0, 165, 14), whitneyBold12)
					graphics.font = whitneyMedium16
					ImageUtils.drawCenteredString(graphics, marriedWith.name + "#" + marriedWith.discriminator, Rectangle(635, 16, 165, 18), whitneyMedium16)
					graphics.font = whitneyBold12
					ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(marriage.marriedSince, System.currentTimeMillis(), locale), Rectangle(635, 16 + 18, 165, 14), whitneyBold12)
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
				7,
				443,
				null
		)
	}

	fun drawBadges(badges: List<BufferedImage>, graphics: Graphics) {
		val treeOrnamentsCoordinates = listOf(
				Pair(13, 80),
				Pair(58, 120),
				Pair(3, 193),
				Pair(67, 210),
				Pair(75, 256),
				Pair(33, 271),
				Pair(131, 290),
				Pair(88, 308),
				Pair(39, 344),
				Pair(127, 344),
				Pair(73, 374),
				Pair(157, 389),
				Pair(116, 402)
		)

		for ((idx, badge) in badges.withIndex()) {
			val coordinate = treeOrnamentsCoordinates.getOrNull(idx) ?: break
			graphics.drawImage(badge, coordinate.first, coordinate.second, null)
		}
	}

	fun drawReputations(user: ProfileUserInfoData, graphics: Graphics, reputations: Long) {
		val font = graphics.font

		ImageUtils.drawCenteredString(graphics, "$reputations reps", Rectangle(634, 454, 166, 52), font)
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

		var y = 515
		for (line in userInfo) {
			graphics.drawText(loritta, line, 773 - biggestStrWidth - 2, y)
			y += 16
		}

		return biggestStrWidth
	}
}