package com.mrpowergamerbr.loritta.profile

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO

class NostalgiaProfileCreator : ProfileCreator {
	override fun create(sender: User, user: User, userProfile: Profile, guild: Guild, serverConfig: ServerConfig, badges: List<BufferedImage>, locale: BaseLocale, background: BufferedImage, aboutMe: String, member: Member?): BufferedImage {
		val profileWrapper = ImageIO.read(File(Loritta.ASSETS, "profile/nostalgia/profile_wrapper.png"))

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB); // Base
		val graphics = base.graphics as java.awt.Graphics2D;
		graphics.setRenderingHint(
				java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
				java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		val avatar = LorittaUtils.downloadImage(user.effectiveAvatarUrl).getScaledInstance(152, 152, BufferedImage.SCALE_SMOOTH)

		graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)

		graphics.drawImage(profileWrapper, 0, 0, null)
		graphics.drawImage(avatar.toBufferedImage().makeRoundedCorners(152), 4, 4, null)

		val oswaldRegular50 = Constants.OSWALD_REGULAR
				.deriveFont(50F)
		val oswaldRegular42 = Constants.OSWALD_REGULAR
				.deriveFont(42F)
		val oswaldRegular29= Constants.OSWALD_REGULAR
				.deriveFont(29F)

		val reputations = transaction(Databases.loritta) {
			com.mrpowergamerbr.loritta.tables.Reputations.select { Reputations.receivedById eq user.idLong }.count()
		}

		graphics.color = Color.WHITE
		graphics.font = oswaldRegular50
		graphics.drawText(user.name, 159, 46) // Nome do usuário
		graphics.font = oswaldRegular42
		ImageUtils.drawCenteredString(graphics, "${reputations} reps", Rectangle(598, 54, 202, 54), oswaldRegular42)
		graphics.font = oswaldRegular29
		ImageUtils.drawCenteredString(graphics, "Sobre Mim", Rectangle(0, 465, 132, 38), oswaldRegular29)

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
		graphics.drawText("${userProfile.xp} XP", 159, 39  + shiftY, 800 - 6)

		val localPosition = serverConfig.guildUserData.sortedByDescending { it.xp }.indexOfFirst { it.userId == user.id } + 1
		val xpLocal = serverConfig.guildUserData.firstOrNull { it.userId == user.id }

		graphics.font = whitneyBold20
		graphics.drawText(guild.name, 159, 61  + shiftY, 800 - 6)
		graphics.font = whitneySemiBold20
		if (xpLocal != null) {
			graphics.drawText("#$localPosition / ${xpLocal.xp} XP", 159, 78 + shiftY, 800 - 6)
		} else {
			graphics.drawText("???", 159, 78 + shiftY, 800 - 6)
		}

		graphics.font = whitneyBold20
		graphics.drawText("Sonhos", 159, 98  + shiftY, 800 - 6)
		graphics.font = whitneySemiBold20
		graphics.drawText(userProfile.money.toString(), 159, 116  + shiftY, 800 - 6)
		val marriage = transaction(Databases.loritta) { userProfile.marriage }

		if (marriage != null) {
			val marriedWithId = if (marriage.user1 == user.idLong) {
				marriage.user2
			} else {
				marriage.user1
			}.toString()

			val marrySection = ImageIO.read(File(Loritta.ASSETS, "profile/nostalgia/marry.png"))
			graphics.drawImage(marrySection, 0, 0, null)
			val marriedWith = lorittaShards.getUserById(marriedWithId)

			if (marriedWith != null) {
				val whitneySemiBold16 = whitneySemiBold.deriveFont(16f)
				val whitneyMedium20 = whitneyMedium22.deriveFont(20f)
				graphics.font = whitneySemiBold16
				ImageUtils.drawCenteredString(graphics, "Casado com", Rectangle(545, 108, 256, 14), whitneySemiBold16)
				graphics.font = whitneyMedium20
				ImageUtils.drawCenteredString(graphics, marriedWith.name + "#" + marriedWith.discriminator, Rectangle(545, 108 + 14, 256, 18), whitneyMedium20)
				graphics.font = whitneySemiBold16
				ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(marriage.marriedSince, System.currentTimeMillis(), locale), Rectangle(545, 108 + 14  + 18, 256, 14), whitneySemiBold16)
			}
		}

		return base.makeRoundedCorners(15)
	}
}