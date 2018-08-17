package com.mrpowergamerbr.loritta.profile

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.User
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO

class MSNProfileCreator : ProfileCreator {
	override fun create(sender: User, user: User, userProfile: LorittaProfile, guild: Guild, serverConfig: ServerConfig, badges: List<BufferedImage>, locale: BaseLocale, background: BufferedImage, aboutMe: String, member: Member?): BufferedImage {
		val profileWrapper = ImageIO.read(File(Loritta.ASSETS, "profile/msn/profile_wrapper.png"))

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB); // Base
		val graphics = base.graphics as java.awt.Graphics2D;
		graphics.setRenderingHint(
				java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
				java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		val avatar = LorittaUtils.downloadImage(user.effectiveAvatarUrl).getScaledInstance(141, 141, BufferedImage.SCALE_SMOOTH)
		val imageToBeDownload = if (sender == user) { guild.selfMember.user.avatarUrl } else { sender.effectiveAvatarUrl }
		val senderAvatar = LorittaUtils.downloadImage(imageToBeDownload).getScaledInstance(141, 141, BufferedImage.SCALE_SMOOTH)

		val msnFont = FileInputStream(File(Loritta.ASSETS + "micross.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
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
		val whitneyMedium32 = whitneySemiBold.deriveFont(32f)
		val whitneyBold20 = whitneyBold.deriveFont(20f)
		val whitneySemiBold20 = whitneySemiBold.deriveFont(20f)

		val msnFont20 = whitneyMedium.deriveFont(20f)
		val msnFont15 = whitneyBold20.deriveFont(17f)
		val msnFont24 = whitneyBold20.deriveFont(24f)

		graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)
		graphics.drawImage(avatar, 70, 130, null)
		graphics.drawImage(senderAvatar, 70, 422, null)

		graphics.drawImage(profileWrapper, 0, 0, null)

		graphics.font = msnFont15
		graphics.color = Color(255, 255, 255)
		graphics.drawText("${user.name}#${user.discriminator} <${user.id}>", 40, 27)
		graphics.font = whitneyMedium32
		// OUTLINE
		graphics.color = Color(51, 51, 51)
		graphics.drawText(user.name, 266, 142)
		graphics.drawText(user.name, 272, 142)
		graphics.drawText(user.name, 269, 139)
		graphics.drawText(user.name, 269, 145)

		// USER NAME
		graphics.color = Color(255, 255, 255)
		graphics.drawText(user.name, 269, 142)

		if (member != null && member.game != null) {
			val gameIcon = ImageIO.read(File(Loritta.ASSETS, "profile/msn/game_icon.png"))
			graphics.drawImage(gameIcon, 0, 5, null)
			graphics.font = msnFont24
			graphics.color = Color(51, 51, 51)
			graphics.drawText(member.game.name, 294, 169)
			graphics.drawText(member.game.name, 296, 169)
			graphics.drawText(member.game.name, 295, 168)
			graphics.drawText(member.game.name, 295, 170)

			// GAME
			graphics.color = Color(255, 255, 255)
			graphics.drawText(member.game.name, 295, 169)
		}

		graphics.font = msnFont20
		graphics.color = Color(142, 124, 125)
		ImageUtils.drawTextWrapSpaces("Não inclua informações como senhas ou número de cartões de crédito em uma mensagem instantânea", 297, 224, 768, 1000, graphics.fontMetrics, graphics)

		ImageUtils.drawTextWrapSpaces("${user.name} diz", 267, 302, 768, 1000, graphics.fontMetrics, graphics)
		ImageUtils.drawTextWrapSpaces(/* "Olá, meu nome é ${user.name}! Atualmente eu tenho ${userProfile.dreams} Sonhos, já recebi ${userProfile.receivedReputations.size} reputações, estou em #$position (${userProfile.xp} XP) no rank global e estou em #$localPosition (${xpLocal?.xp} XP) no rank do ${guild.name}!\n\n${userProfile.aboutMe}" */ aboutMe, 297, 326, 768, 1000, graphics.fontMetrics, graphics)

		val position = loritta.usersColl.find(Filters.gt("xp", userProfile.xp)).count() + 1

		val shiftY = 291

		graphics.font = whitneyBold20
		graphics.drawText("Global", 4, 21 + shiftY, 244)
		graphics.font = whitneySemiBold20
		graphics.drawText("#$position / ${userProfile.xp} XP", 4, 39  + shiftY, 244)

		val localPosition = serverConfig.guildUserData.sortedByDescending { it.xp }.indexOfFirst { it.userId == userProfile.userId } + 1
		val xpLocal = serverConfig.guildUserData.firstOrNull { it.userId == userProfile.userId }

		graphics.font = whitneyBold20
		graphics.drawText(guild.name, 4, 61  + shiftY, 244)
		graphics.font = whitneySemiBold20
		if (xpLocal != null) {
			graphics.drawText("#$localPosition / ${xpLocal.xp} XP", 4, 78 + shiftY, 244)
		} else {
			graphics.drawText("???", 4, 78 + shiftY, 244)
		}

		graphics.font = whitneyBold20
		graphics.drawText("Sonhos", 4, 98  + shiftY, 244)
		graphics.font = whitneySemiBold20
		graphics.drawText(userProfile.dreams.toString(), 4, 116  + shiftY, 244)

		var x = 272
		for (badge in badges) {
			graphics.drawImage(badge.getScaledInstance(29, 29, BufferedImage.SCALE_SMOOTH), x, 518, null)
			x += 35
		}

		return base
	}
}