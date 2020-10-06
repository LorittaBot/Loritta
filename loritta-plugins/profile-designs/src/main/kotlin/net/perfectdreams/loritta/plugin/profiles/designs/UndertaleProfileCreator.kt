package net.perfectdreams.loritta.plugin.profiles.designs

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.profile.ProfileCreator
import com.mrpowergamerbr.loritta.profile.ProfileUserInfoData
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class UndertaleProfileCreator : ProfileCreator("undertaleBattle") {
	override suspend fun create(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, badges: List<BufferedImage>, locale: LegacyBaseLocale, background: BufferedImage, aboutMe: String, member: Member?): BufferedImage {
		val profileWrapper = ImageIO.read(File(Loritta.ASSETS, "profile/undertale/profile_wrapper.png"))

		val determinationMono = Constants.DETERMINATION_MONO

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

		graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)

		graphics.font = determinationMono.deriveFont(Font.PLAIN, 22f)
		graphics.color = Color.WHITE

		val avatar = LorittaUtils.downloadImage(user.avatarUrl)!!.getScaledInstance(159, 159, BufferedImage.SCALE_SMOOTH)

		graphics.drawImage(profileWrapper, 0, 0, null)

		drawAvatar(avatar, graphics)

		graphics.drawText("* ${user.name} appears!", 56, 347) // Nome do usuário

		drawReputations(user, graphics)

		drawBadges(badges, graphics)

		drawMarriageStatus(userProfile, locale.toNewLocale(), graphics)

		val biggestStrWidth = drawUserInfo(user, userProfile, guild, graphics)

		ImageUtils.drawTextWrapSpaces(aboutMe, 56, 375, 751 - biggestStrWidth - 4, 600, graphics.fontMetrics, graphics)

		return base.makeRoundedCorners(15)
	}

	fun drawAvatar(avatar: Image, graphics: Graphics) {
		graphics.drawImage(
				avatar.toBufferedImage(),
				321,
				153,
				null
		)
	}

	fun drawBadges(badges: List<BufferedImage>, graphics: Graphics) {
		var y = 3
		for (badge in badges) {
			graphics.drawImage(badge.getScaledInstance(32, 32, BufferedImage.SCALE_SMOOTH), 3, y, null)
			y += 35
		}
	}

	fun drawReputations(user: ProfileUserInfoData, graphics: Graphics) {
		val font = graphics.font
		val reputations = ProfileUtils.getReputationCount(user)

		ImageUtils.drawCenteredString(graphics, "$reputations reps", Rectangle(654, 546, 104, 37), font)
	}

	fun drawMarriageStatus(userProfile: Profile, locale: BaseLocale, graphics: Graphics) {
		ProfileUtils.getMarriageInfo(userProfile)?.let { (marriage, marriedWith) ->
			val font = graphics.font
			val marriedWithText = "${locale["profile.marriedWith"]} ${marriedWith.name}#${marriedWith.discriminator}"

			ImageUtils.drawCenteredString(graphics, marriedWithText, Rectangle(42, 543, 522, 47), font)
		}
	}

	fun drawUserInfo(user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, graphics: Graphics): Int {
		val userInfo = mutableListOf<String>()
		userInfo.add("Global")
		val globalPosition = ProfileUtils.getGlobalExperiencePosition(userProfile)
		userInfo.add("#$globalPosition / ${userProfile.xp} XP")

		if (guild != null) {
			val localProfile = ProfileUtils.getLocalProfile(guild, user)

			val localPosition = ProfileUtils.getLocalExperiencePosition(localProfile)

			val xpLocal = localProfile?.xp

			// Iremos remover os emojis do nome da guild, já que ele não calcula direito no stringWidth
			userInfo.add(guild.name.replace(Constants.EMOJI_PATTERN.toRegex(), ""))
			if (xpLocal != null) {
				userInfo.add("#$localPosition / $xpLocal XP")
			} else {
				userInfo.add("???")
			}
		}

		val globalEconomyPosition = ProfileUtils.getGlobalEconomyPosition(userProfile)

		userInfo.add("Sonhos")
		userInfo.add("#$globalEconomyPosition / ${userProfile.money}")

		val biggestStrWidth = graphics.fontMetrics.stringWidth(userInfo.maxBy { graphics.fontMetrics.stringWidth(it) }!!)

		var y = 347
		for (line in userInfo) {
			graphics.drawText(line, 749 - biggestStrWidth - 2, y)
			y += 18
		}

		return biggestStrWidth
	}
}