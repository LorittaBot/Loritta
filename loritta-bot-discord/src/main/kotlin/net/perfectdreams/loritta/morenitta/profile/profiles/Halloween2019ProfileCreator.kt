package net.perfectdreams.loritta.morenitta.profile.profiles

import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
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

class Halloween2019ProfileCreator(loritta: LorittaBot) : AnimatedProfileCreator(loritta, "halloween2019") {
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
	): List<BufferedImage> {
		val list = mutableListOf<BufferedImage>()

		val latoBold = loritta.graphicsFonts.latoBold
		val latoBlack = loritta.graphicsFonts.latoBlack

		val latoRegular22 = latoBold.deriveFont(22f)
		val latoBlack16 = latoBlack.deriveFont(16f)
		val latoRegular16 = latoBold.deriveFont(16f)
		val latoBlack12 = latoBlack.deriveFont(12f)
		val oswaldRegular50 = Constants.OSWALD_REGULAR
			.deriveFont(50F)
		val oswaldRegular42 = Constants.OSWALD_REGULAR
			.deriveFont(42F)

		val avatar = (LorittaUtils.downloadImage(loritta, user.avatarUrl) ?: Constants.DEFAULT_DISCORD_BLUE_AVATAR).getScaledInstance(152, 152, BufferedImage.SCALE_SMOOTH)
		val marrySection = readImageFromResources("/profile/halloween_2019/marry.png")

		val marriage = ProfileUtils.getMarriageInfo(loritta, userProfile)

		val reputations = ProfileUtils.getReputationCount(loritta, user)

		var xpLocal: Long? = null
		var localPosition: Long? = null

		if (guild != null) {
			val localProfile = ProfileUtils.getLocalProfile(loritta, guild, user)

			localPosition = ProfileUtils.getLocalExperiencePosition(loritta, localProfile)
			xpLocal = localProfile?.xp
		}

		val globalEconomyPosition = ProfileUtils.getGlobalEconomyPosition(loritta, userProfile)

		val resizedBadges = badges.map { it.getScaledInstance(35, 35, BufferedImage.SCALE_SMOOTH).toBufferedImage() }

		for (i in 0..29) {
			val profileWrapper = readImageFromResources("/profile/halloween_2019/frames/halloween_2019_${i.toString().padStart(6, '0')}.png")

			val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
			val graphics = base.graphics.enableFontAntiAliasing()

			graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)

			graphics.color = Color.BLACK
			graphics.drawImage(profileWrapper, 0, 0, null)
			drawAvatar(avatar, graphics)

			graphics.font = oswaldRegular50
			graphics.drawText(loritta, user.name, 162, 461) // Nome do usuário
			graphics.font = oswaldRegular42

			drawReputations(user, graphics, reputations)

			drawBadges(resizedBadges, graphics)

			graphics.font = latoBlack16
			val biggestStrWidth = drawUserInfo(user, userProfile, guild, graphics, null, localPosition, xpLocal, globalEconomyPosition)

			graphics.font = latoRegular22

			drawAboutMeWrapSpaces(graphics, graphics.fontMetrics, aboutMe, 162, 484, 773 - biggestStrWidth - 4, 600, allowedDiscordEmojis)

			if (marriage != null) {
				graphics.drawImage(marrySection, 0, 0, null)

				graphics.color = Color.WHITE
				graphics.font = latoBlack12
				ImageUtils.drawCenteredString(graphics, locale["profile.marriedWith"], Rectangle(635, 350, 165, 14), latoBlack12)
				graphics.font = latoRegular16
				ImageUtils.drawCenteredString(graphics, marriage.partner.name, Rectangle(635, 350 + 16, 165, 18), latoRegular16)
				graphics.font = latoBlack12
				ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(i18nContext, marriage.marriage.marriedSince.toEpochMilliseconds(), System.currentTimeMillis(), 3), Rectangle(635, 350 + 16 + 18, 165, 14), latoBlack12)
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

	fun drawUserInfo(user: ProfileUserInfoData, userProfile: Profile, guild: ProfileGuildInfoData?, graphics: Graphics, globalPosition: Long?, localPosition: Long?, xpLocal: Long?, globalEconomyPosition: Long?): Int {
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
			graphics.drawText(loritta, line, 773 - biggestStrWidth - 2, y)
			y += 16
		}

		return biggestStrWidth
	}
}