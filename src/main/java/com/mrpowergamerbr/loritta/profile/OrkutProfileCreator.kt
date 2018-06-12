package com.mrpowergamerbr.loritta.profile

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

class OrkutProfileCreator : ProfileCreator {
	override fun create(sender: User, user: User, userProfile: LorittaProfile, guild: Guild, serverConfig: ServerConfig, badges: List<BufferedImage>, locale: BaseLocale, background: BufferedImage, aboutMe: String, member: Member?): BufferedImage {
		val profileWrapper = ImageIO.read(File(Loritta.ASSETS, "profile/orkut/profile_wrapper.png"))

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB); // Base
		val graphics = base.graphics as java.awt.Graphics2D;
		graphics.setRenderingHint(
				java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
				java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		val avatar = LorittaUtils.downloadImage(user.effectiveAvatarUrl).getScaledInstance(200, 200, BufferedImage.SCALE_SMOOTH)

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
		val whitneyMedium18 = whitneyMedium.deriveFont(18f)
		val whitneyBold20 = whitneyBold.deriveFont(20f)
		val whitneySemiBold18 = whitneySemiBold.deriveFont(18f)
		val whitneyMedium16 = whitneyMedium.deriveFont(16f)

		val msnFont20 = whitneyMedium.deriveFont(20f)
		val msnFont15 = whitneyBold20.deriveFont(17f)
		val msnFont24 = whitneyBold20.deriveFont(24f)

		graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)
		graphics.drawImage(profileWrapper, 0, 0, null)

		graphics.drawImage(avatar, 8, 8, null)

		graphics.color = Color(102, 102, 102)
		graphics.font = whitneySemiBold18
		graphics.drawText(user.name, 14, 251, 201)
		ImageUtils.drawTextWrapSpaces(aboutMe, 256, 51, 773, 1000, graphics.fontMetrics, graphics)

		graphics.color = Color.BLACK
		graphics.drawString("Sua sorte do dia", 267, 257)

		graphics.font = whitneyMedium18
		graphics.drawString("A Loritta e a Pantufa são suas amigas", 267, 276)

		val reversedRep = userProfile.receivedReputations.size.toString().reversed()

		var startX = 202
		graphics.color = Color(153, 153, 153)
		for (ch in reversedRep) {
			startX -= graphics.fontMetrics.charWidth(ch)
			graphics.drawString(ch.toString(), startX, 291)
		}

		val mutualGuildsByUsers = lorittaShards.getMutualGuilds(user).filter { it.iconUrl != null }.sortedByDescending { it.members.size }

		var startGuildX = 252
		var startGuildY = 400

		graphics.color = Color(42, 127, 212)
		graphics.font = whitneyMedium16
		for ((index, guild) in mutualGuildsByUsers.withIndex()) {
			if (index == 8)
				break
			if (index == 4) {
				startGuildX = 252
				startGuildY += 113 + 26
			}

			val icon = LorittaUtils.downloadImage(guild.iconUrl).getScaledInstance(113, 113, BufferedImage.SCALE_SMOOTH)
			graphics.drawImage(icon, startGuildX, startGuildY, null)
			graphics.drawText(guild.name, startGuildX, startGuildY + 113 + 18, startGuildX + 113)
			startGuildX += 113 + 12
		}

		graphics.color = Color(51, 51, 93)
		graphics.drawString("Comunidades (${mutualGuildsByUsers.size})", 226, 332)
		return base
	}
}