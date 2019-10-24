package net.perfectdreams.loritta.plugin.profiles.designs

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.profile.ProfileCreator
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
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

class MonicaAtaProfileCreator : ProfileCreator {
	val KOMIKA by lazy {
		FileInputStream(File(Loritta.ASSETS + "komika.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
	}

	override fun create(sender: User, user: User, userProfile: Profile, guild: Guild, serverConfig: MongoServerConfig, badges: List<BufferedImage>, locale: LegacyBaseLocale, background: BufferedImage, aboutMe: String, member: Member?): BufferedImage {
		val profileWrapper = ImageIO.read(File(Loritta.ASSETS, "profile/monica_ata/profile_wrapper.png"))

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

		val userInfo = mutableListOf<String>()
		userInfo.add("Global")
		val globalPosition = transaction(Databases.loritta) {
			Profiles.select { Profiles.xp greaterEq userProfile.xp }.count()
		}
		userInfo.add("#$globalPosition / ${userProfile.xp} XP")

		val localProfile = transaction(Databases.loritta) {
			GuildProfile.find { (GuildProfiles.guildId eq guild.idLong) and (GuildProfiles.userId eq user.idLong) }.firstOrNull()
		}

		val localPosition = if (localProfile != null) {
			transaction(Databases.loritta) {
				GuildProfiles.select { (GuildProfiles.guildId eq guild.idLong) and (GuildProfiles.xp greaterEq localProfile.xp) }.count()
			}
		} else { null }

		val xpLocal = localProfile?.xp

		// Iremos remover os emojis do nome da guild, já que ele não calcula direito no stringWidth
		userInfo.add(guild.name.replace(Constants.EMOJI_PATTERN.toRegex(), ""))
		if (xpLocal != null) {
			userInfo.add("#$localPosition / $xpLocal XP")
		} else {
			userInfo.add("???")
		}

		val globalEconomyPosition = transaction(Databases.loritta) {
			Profiles.select { Profiles.money greaterEq userProfile.money }.count()
		}

		userInfo.add("Sonhos")
		userInfo.add("#$globalEconomyPosition / ${"%.2f".format(userProfile.money)}")

		graphics.font = KOMIKA.deriveFont(13f)
		val biggestStrWidth = graphics.fontMetrics.stringWidth(userInfo.maxBy { graphics.fontMetrics.stringWidth(it) }!!)

		val avatar = LorittaUtils.downloadImage(user.effectiveAvatarUrl)!!.getScaledInstance(148, 148, BufferedImage.SCALE_SMOOTH)

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

		val reputations = transaction(Databases.loritta) {
			Reputations.select { Reputations.receivedById eq user.idLong }.count()
		}

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

		val marriage = transaction(Databases.loritta) { userProfile.marriage }

		if (marriage != null) {
			val marriedWithId = if (marriage.user1 == user.idLong) {
				marriage.user2
			} else {
				marriage.user1
			}.toString()

			val marrySection = ImageIO.read(File(Loritta.ASSETS, "profile/monica_ata/marry.png"))
			graphics.drawImage(marrySection, 0, 0, null)
			val marriedWith = runBlocking { lorittaShards.retrieveUserById(marriedWithId) }

			if (marriedWith != null) {
				graphics.font = KOMIKA.deriveFont(21f)
				ImageUtils.drawCenteredString(graphics, locale.toNewLocale()["profile.marriedWith"], Rectangle(280, 270, 218, 22), graphics.font)
				graphics.font = KOMIKA.deriveFont(16f)
				ImageUtils.drawCenteredString(graphics, marriedWith.name + "#" + marriedWith.discriminator, Rectangle(280, 270 + 23, 218, 18), graphics.font)
				graphics.font = KOMIKA.deriveFont(12f)
				ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(marriage.marriedSince, System.currentTimeMillis(), locale), Rectangle(280, 270 + 23 + 16, 218, 15), graphics.font)
			}
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