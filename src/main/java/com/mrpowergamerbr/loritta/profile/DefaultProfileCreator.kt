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
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO

class DefaultProfileCreator : ProfileCreator {
	override fun create(sender: User, user: User, userProfile: LorittaProfile, guild: Guild, serverConfig: ServerConfig, badges: List<BufferedImage>, locale: BaseLocale, background: BufferedImage, aboutMe: String, member: Member?): BufferedImage {
		val profileWrapper = ImageIO.read(File(Loritta.ASSETS, "profile_wrapper_v4.png"))
		val profileWrapperOverlay = ImageIO.read(File(Loritta.ASSETS, "profile_wrapper_v4_overlay.png"))
		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB); // Base
		val graphics = base.graphics as java.awt.Graphics2D;
		graphics.setRenderingHint(
				java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
				java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		val avatar = LorittaUtils.downloadImage(user.effectiveAvatarUrl).getScaledInstance(115, 115, BufferedImage.SCALE_SMOOTH)

		graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)

		graphics.drawImage(profileWrapper, 0, 0, null)
		graphics.drawImage(avatar.toBufferedImage().makeRoundedCorners(115), 6, 6, null)

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

		graphics.font = whitneySemiBold38

		if (badges.isEmpty()) {
			graphics.drawText(user.name, 139, 71, 517 - 6)
		} else { // Caso exista badges, nós iremos alterar um pouquinho aonde o nome é desenhado
			graphics.drawText(user.name, 139, 61 - 4, 517 - 6)
			var x = 139
			// E agora desenhar as badges
			badges.forEach {
				val badge = it.getScaledInstance(27, 27, BufferedImage.SCALE_SMOOTH)
				graphics.drawImage(badge, x, 66 + 4, null)
				x += 27 + 8
			}
		}

		val position = loritta.usersColl.find(Filters.gt("xp", userProfile.xp)).count() + 1

		val guildIcon = LorittaUtils.downloadImage(guild?.iconUrl?.replace("jpg", "png") ?: "https://emojipedia-us.s3.amazonaws.com/thumbs/320/google/56/shrug_1f937.png").getScaledInstance(38, 38, BufferedImage.SCALE_SMOOTH)

		graphics.font = whitneyBold20
		graphics.drawText("Global", 562, 21, 800 - 6)
		graphics.font = whitneySemiBold20
		graphics.drawText("#$position / ${userProfile.xp} XP", 562, 39, 800 - 6)

		val localPosition = serverConfig.guildUserData.sortedByDescending { it.xp }.indexOfFirst { it.userId == userProfile.userId } + 1
		val xpLocal = serverConfig.guildUserData.firstOrNull { it.userId == userProfile.userId }

		graphics.font = whitneyBold20
		graphics.drawText(guild.name, 562, 61, 800 - 6)
		graphics.font = whitneySemiBold20
		if (xpLocal != null) {
			graphics.drawText("#$localPosition / ${xpLocal.xp} XP", 562, 78, 800 - 6)
		} else {
			graphics.drawText("???", 562, 78, 800 - 6)
		}

		graphics.font = whitneyBold20
		graphics.drawText("Reputação", 562, 102, 800 - 6)
		graphics.font = whitneySemiBold20
		graphics.drawText("${userProfile.receivedReputations.size} reps", 562, 120, 800 - 6)

		graphics.font = whitneyBold20
		graphics.drawText(locale["ECONOMY_NamePlural"], 562, 492, 800 - 6)
		graphics.font = whitneySemiBold20
		graphics.drawText("${userProfile.dreams}", 562, 511, 800 - 6)

		graphics.drawImage(guildIcon.toBufferedImage().makeRoundedCorners(38), 520, 44, null)
		graphics.font = whitneyMedium22

		ImageUtils.drawTextWrapSpaces(aboutMe, 6, 493, 517 - 6, 600, graphics.fontMetrics, graphics)

		graphics.drawImage(profileWrapperOverlay, 0, 0, null)

		return base.makeRoundedCorners(15)
	}
}