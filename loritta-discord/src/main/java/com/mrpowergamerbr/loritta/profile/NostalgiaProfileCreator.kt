package com.mrpowergamerbr.loritta.profile

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO

class NostalgiaProfileCreator : ProfileCreator("defaultDark") {
	override suspend fun create(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild??, badges: List<BufferedImage>, locale: LegacyBaseLocale, background: BufferedImage, aboutMe: String): BufferedImage {
		val profileWrapper = ImageIO.read(File(Loritta.ASSETS, "profile/nostalgia/profile_wrapper.png"))

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

		val avatar = LorittaUtils.downloadImage(user.avatarUrl)!!.getScaledInstance(152, 152, BufferedImage.SCALE_SMOOTH)

		graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)

		graphics.drawImage(profileWrapper, 0, 0, null)
		graphics.drawImage(avatar.toBufferedImage().makeRoundedCorners(152), 4, 4, null)

		val oswaldRegular50 = Constants.OSWALD_REGULAR
				.deriveFont(50F)
		val oswaldRegular42 = Constants.OSWALD_REGULAR
				.deriveFont(42F)
		val oswaldRegular29= Constants.OSWALD_REGULAR
				.deriveFont(29F)

		val reputations = loritta.newSuspendedTransaction {
			Reputations.select { Reputations.receivedById eq user.id }.count()
		}

		graphics.color = Color.WHITE
		graphics.font = oswaldRegular50
		graphics.drawText(user.name, 159, 46) // Nome do usuário
		graphics.font = oswaldRegular42
		ImageUtils.drawCenteredString(graphics, "${reputations} reps", Rectangle(598, 54, 202, 54), oswaldRegular42)
		graphics.font = oswaldRegular29
		ImageUtils.drawCenteredString(graphics, locale.toNewLocale()["profile.aboutMe"], Rectangle(0, 465, 132, 38), oswaldRegular29)

		var x = 162
		for (badge in badges) {
			graphics.drawImage(badge.getScaledInstance(34, 34, BufferedImage.SCALE_SMOOTH), x, 466, null)
			x += 42
		}

		val whitneyMedium = 	FileInputStream(File(Loritta.ASSETS + "whitney-medium.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
		val whitneySemiBold = 	FileInputStream(File(Loritta.ASSETS + "whitney-semibold.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
		val whitneyBold = 	FileInputStream(File(Loritta.ASSETS + "whitney-bold.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}

		val whitneySemiBold38 = whitneySemiBold.deriveFont(38f)
		val whitneyMedium22 = whitneySemiBold.deriveFont(22f)
		val whitneyBold20 = whitneyBold.deriveFont(20f)
		val whitneySemiBold20 = whitneySemiBold.deriveFont(20f)

		graphics.font = whitneyMedium22

		ImageUtils.drawTextWrapSpaces(aboutMe, 6, 522, 800 - 6, 600, graphics.fontMetrics, graphics)

		val shiftY = 42

		graphics.font = whitneyBold20
		graphics.drawText("Global", 159, 21 + shiftY, 800 - 6)
		graphics.font = whitneySemiBold20
		val globalPosition = loritta.newSuspendedTransaction {
			Profiles.select { Profiles.xp greaterEq userProfile.xp }.count()
		}
		graphics.drawText("#$globalPosition / ${userProfile.xp} XP", 159, 39  + shiftY, 800 - 6)

		if (guild != null) {
			val localProfile = loritta.newSuspendedTransaction {
				GuildProfile.find { (GuildProfiles.guildId eq guild.idLong) and (GuildProfiles.userId eq user.id) }.firstOrNull()
			}

			val localPosition = if (localProfile != null) {
				loritta.newSuspendedTransaction {
					GuildProfiles.select { (GuildProfiles.guildId eq guild.idLong) and (GuildProfiles.xp greaterEq localProfile.xp) }.count()
				}
			} else {
				null
			}

			val xpLocal = localProfile?.xp

			graphics.font = whitneyBold20
			graphics.drawText(guild.name, 159, 61 + shiftY, 800 - 6)
			graphics.font = whitneySemiBold20
			if (xpLocal != null) {
				graphics.drawText("#$localPosition / $xpLocal XP", 159, 78 + shiftY, 800 - 6)
			} else {
				graphics.drawText("???", 159, 78 + shiftY, 800 - 6)
			}
		}

		val globalEconomyPosition = loritta.newSuspendedTransaction {
			Profiles.select { Profiles.money greaterEq userProfile.money }.count()
		}

		graphics.font = whitneyBold20
		graphics.drawText("Sonhos", 159, 98  + shiftY, 800 - 6)
		graphics.font = whitneySemiBold20
		graphics.drawText("#$globalEconomyPosition / ${userProfile.money}", 159, 116  + shiftY, 800 - 6)
		val marriage = loritta.newSuspendedTransaction { userProfile.marriage }

		if (marriage != null) {
			val marriedWithId = if (marriage.user1 == user.id) {
				marriage.user2
			} else {
				marriage.user1
			}.toString()

			val marrySection = ImageIO.read(File(Loritta.ASSETS, "profile/nostalgia/marry.png"))
			graphics.drawImage(marrySection, 0, 0, null)
			val marriedWith = runBlocking { lorittaShards.retrieveUserInfoById(marriedWithId.toLong()) }

			if (marriedWith != null) {
				val whitneySemiBold16 = whitneySemiBold.deriveFont(16f)
				val whitneyMedium20 = whitneyMedium22.deriveFont(20f)
				graphics.font = whitneySemiBold16
				ImageUtils.drawCenteredString(graphics, locale.toNewLocale()["profile.marriedWith"], Rectangle(545, 108, 256, 14), whitneySemiBold16)
				graphics.font = whitneyMedium20
				ImageUtils.drawCenteredString(graphics, marriedWith.name + "#" + marriedWith.discriminator, Rectangle(545, 108 + 14, 256, 18), whitneyMedium20)
				graphics.font = whitneySemiBold16
				ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(marriage.marriedSince, System.currentTimeMillis(), locale), Rectangle(545, 108 + 14  + 18, 256, 14), whitneySemiBold16)
			}
		}

		return base
	}
}