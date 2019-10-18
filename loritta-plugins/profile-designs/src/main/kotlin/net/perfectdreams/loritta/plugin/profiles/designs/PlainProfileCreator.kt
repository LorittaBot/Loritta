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
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO

open class PlainProfileCreator(val color: String) : ProfileCreator {
	class PlainWhiteProfileCreator : PlainProfileCreator("white")
	class PlainOrangeProfileCreator : PlainProfileCreator("orange")
	class PlainPurpleProfileCreator : PlainProfileCreator("purple")
	class PlainAquaProfileCreator : PlainProfileCreator("aqua")
	class PlainGreenProfileCreator : PlainProfileCreator("green")

	override fun create(sender: User, user: User, userProfile: Profile, guild: Guild, serverConfig: MongoServerConfig, badges: List<BufferedImage>, locale: LegacyBaseLocale, background: BufferedImage, aboutMe: String, member: Member?): BufferedImage {
		val profileWrapper = ImageIO.read(File(Loritta.ASSETS, "profile/plain/profile_wrapper_$color.png"))

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

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics as java.awt.Graphics2D
		graphics.setRenderingHint(
				java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
				java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON
		)

		val avatar = LorittaUtils.downloadImage(user.effectiveAvatarUrl)!!.getScaledInstance(152, 152, BufferedImage.SCALE_SMOOTH)

		graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)

		val marriage = transaction(Databases.loritta) { userProfile.marriage }

		if (marriage != null) {
			val marriedWithId = if (marriage.user1 == user.idLong) {
				marriage.user2
			} else {
				marriage.user1
			}.toString()

			val marrySection = ImageIO.read(File(Loritta.ASSETS, "profile/plain/marry.png"))
			graphics.drawImage(marrySection, 0, 0, null)
			val marriedWith = runBlocking { lorittaShards.retrieveUserById(marriedWithId) }

			if (marriedWith != null) {
				val whitneySemiBold16 = whitneySemiBold.deriveFont(16f)
				val whitneyMedium20 = whitneyMedium22.deriveFont(20f)
				graphics.color = Color.WHITE
				graphics.font = whitneyBold12
				ImageUtils.drawCenteredString(graphics, locale.toNewLocale()["profile.marriedWith"], Rectangle(635, 350, 165, 14), whitneyBold12)
				graphics.font = whitneyMedium16
				ImageUtils.drawCenteredString(graphics, marriedWith.name + "#" + marriedWith.discriminator, Rectangle(635, 350 + 16, 165, 18), whitneyMedium16)
				graphics.font = whitneyBold12
				ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(marriage.marriedSince, System.currentTimeMillis(), locale), Rectangle(635, 350 + 16 + 18, 165, 14), whitneyBold12)
			}
		}

		graphics.color = Color.BLACK
		graphics.drawImage(profileWrapper, 0, 0, null)
		drawAvatar(avatar, graphics)

		val oswaldRegular50 = Constants.OSWALD_REGULAR
				.deriveFont(50F)
		val oswaldRegular42 = Constants.OSWALD_REGULAR
				.deriveFont(42F)

		graphics.font = oswaldRegular50
		graphics.drawText(user.name, 162, 461) // Nome do usuário
		graphics.font = oswaldRegular42

		drawReputations(user, graphics)

		drawBadges(badges, graphics)

		graphics.font = whitneyBold16
		val biggestStrWidth = drawUserInfo(user, userProfile, guild, graphics)

		graphics.font = whitneyMedium22

		ImageUtils.drawTextWrapSpaces(aboutMe, 162, 484, 773 - biggestStrWidth - 4, 600, graphics.fontMetrics, graphics)

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

	fun drawReputations(user: User, graphics: Graphics) {
		val font = graphics.font
		val reputations = transaction(Databases.loritta) {
			Reputations.select { Reputations.receivedById eq user.idLong }.count()
		}

		ImageUtils.drawCenteredString(graphics, "$reputations reps", Rectangle(634, 404, 166, 52), font)
	}

	fun drawUserInfo(user: User, userProfile: Profile, guild: Guild, graphics: Graphics): Int {
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

		val biggestStrWidth = graphics.fontMetrics.stringWidth(userInfo.maxBy { graphics.fontMetrics.stringWidth(it) }!!)

		var y = 475
		for (line in userInfo) {
			graphics.drawText(line, 773 - biggestStrWidth - 2, y)
			y += 16
		}

		return biggestStrWidth
	}
}