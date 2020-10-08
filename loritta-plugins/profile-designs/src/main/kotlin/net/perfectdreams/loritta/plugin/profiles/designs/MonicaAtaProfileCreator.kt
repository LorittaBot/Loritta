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
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO

class MonicaAtaProfileCreator : ProfileCreator("monicaAta") {
	val KOMIKA by lazy {
		FileInputStream(File(Loritta.ASSETS + "komika.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
	}

	override suspend fun create(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, badges: List<BufferedImage>, locale: LegacyBaseLocale, background: BufferedImage, aboutMe: String): BufferedImage {
		val profileWrapper = ImageIO.read(File(Loritta.ASSETS, "profile/monica_ata/profile_wrapper.png"))

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

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

		graphics.font = KOMIKA.deriveFont(13f)
		val biggestStrWidth = graphics.fontMetrics.stringWidth(userInfo.maxBy { graphics.fontMetrics.stringWidth(it) }!!)

		val avatar = LorittaUtils.downloadImage(user.avatarUrl)!!.getScaledInstance(148, 148, BufferedImage.SCALE_SMOOTH)

		val image = LorittaImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH).toBufferedImage())

		image.setCorners(
				280f, 0f,
				800f, 0f,
				800f, 417f,
				289f, 331f
		)

		graphics.drawImage(image.bufferedImage, 0, 0, null)
		graphics.drawImage(profileWrapper, 0, 0, null)
		graphics.drawImage(avatar.toBufferedImage().makeRoundedCorners(148), 6, 446, null)

		graphics.font = KOMIKA.deriveFont(27f)
		graphics.color = Color.BLACK
		graphics.drawText(user.name, 161, 509, 527)
		graphics.font = KOMIKA.deriveFont(16f)
		graphics.drawStringWrap(aboutMe, 161, 532, 773 - biggestStrWidth - 4)

		val reputations = ProfileUtils.getReputationCount(user)

		graphics.font = KOMIKA.deriveFont(32f)

		ImageUtils.drawCenteredString(graphics, "${reputations} reps", Rectangle(552, 440, 228, 54), graphics.font)

		if (badges.isNotEmpty()) {
			val badgesBackground = ImageIO.read(File(Loritta.ASSETS, "profile/monica_ata/badges.png"))
			graphics.drawImage(badgesBackground, 0, 0, null)

			var x = 196
			for (badge in badges) {
				graphics.drawImage(badge.getScaledInstance(27, 27, BufferedImage.SCALE_SMOOTH), x, 447, null)
				x += 29
			}
		}

		ProfileUtils.getMarriageInfo(userProfile)?.let { (marriage, marriedWith) ->
			val marrySection = ImageIO.read(File(Loritta.ASSETS, "profile/monica_ata/marry.png"))
			graphics.drawImage(marrySection, 0, 0, null)

			graphics.font = KOMIKA.deriveFont(21f)
			ImageUtils.drawCenteredString(graphics, locale.toNewLocale()["profile.marriedWith"], Rectangle(280, 270, 218, 22), graphics.font)
			graphics.font = KOMIKA.deriveFont(16f)
			ImageUtils.drawCenteredString(graphics, marriedWith.name + "#" + marriedWith.discriminator, Rectangle(280, 270 + 23, 218, 18), graphics.font)
			graphics.font = KOMIKA.deriveFont(12f)
			ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(marriage.marriedSince, System.currentTimeMillis(), locale), Rectangle(280, 270 + 23 + 16, 218, 15), graphics.font)
		}

		graphics.font = KOMIKA.deriveFont(13f)
		var y = 513
		for (line in userInfo) {
			graphics.drawText(line, 773 - biggestStrWidth - 2, y)
			y += 14
		}

		return base.makeRoundedCorners(15)
	}
}