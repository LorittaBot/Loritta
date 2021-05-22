package net.perfectdreams.loritta.plugin.profiles.designs

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.profile.ProfileCreator
import com.mrpowergamerbr.loritta.profile.ProfileUserInfoData
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.drawText
import com.mrpowergamerbr.loritta.utils.enableFontAntiAliasing
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.profile.ProfileUtils
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream

class MSNProfileCreator : ProfileCreator("msn") {
	override suspend fun create(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, badges: List<BufferedImage>, locale: BaseLocale, background: BufferedImage, aboutMe: String): BufferedImage {
		val profileWrapper = readImage(File(Loritta.ASSETS, "profile/msn/profile_wrapper.png"))

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

		val avatar = LorittaUtils.downloadImage(user.avatarUrl)!!.getScaledInstance(141, 141, BufferedImage.SCALE_SMOOTH)
		val imageToBeDownload = if (sender == user) { guild?.selfMember?.user?.avatarUrl } else { sender.avatarUrl } ?: "https://cdn.discordapp.com/embed/avatars/0.png?size=256"
		val senderAvatar = LorittaUtils.downloadImage(imageToBeDownload!!)!!.getScaledInstance(141, 141, BufferedImage.SCALE_SMOOTH)

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

		ProfileUtils.getMarriageInfo(userProfile)?.let { (marriage, marriedWith) ->
			val marriedWithText = "${locale["profile.marriedWith"]} ${marriedWith.name}#${marriedWith.discriminator}"
			val gameIcon = readImage(File(Loritta.ASSETS, "profile/msn/game_icon.png"))
			graphics.drawImage(gameIcon, 0, 5, null)
			graphics.font = msnFont24
			graphics.color = Color(51, 51, 51)
			graphics.drawText(marriedWithText, 294, 169)
			graphics.drawText(marriedWithText, 296, 169)
			graphics.drawText(marriedWithText, 295, 168)
			graphics.drawText(marriedWithText, 295, 170)

			// GAME
			graphics.color = Color(255, 255, 255)
			graphics.drawText(marriedWithText, 295, 169)
		}

		graphics.font = msnFont20
		graphics.color = Color(142, 124, 125)
		ImageUtils.drawTextWrapSpaces("Não inclua informações como senhas ou número de cartões de crédito em uma mensagem instantânea", 297, 224, 768, 1000, graphics.fontMetrics, graphics)

		ImageUtils.drawTextWrapSpaces("${user.name} diz", 267, 302, 768, 1000, graphics.fontMetrics, graphics)
		ImageUtils.drawTextWrapSpaces(/* "Olá, meu nome é ${user.name}! Atualmente eu tenho ${userProfile.dreams} Sonhos, já recebi ${userProfile.receivedReputations.size} reputações, estou em #$position (${userProfile.xp} XP) no rank global e estou em #${localPosition ?: "???"} (${xpLocal?.xp} XP) no rank do ${guild.name}!\n\n${userProfile.aboutMe}" */ aboutMe, 297, 326, 768, 1000, graphics.fontMetrics, graphics)

		val shiftY = 291

		graphics.font = whitneyBold20
		val globalPosition = ProfileUtils.getGlobalExperiencePosition(userProfile)
		graphics.drawText("Global", 4, 21 + shiftY, 244)
		graphics.font = whitneySemiBold20

		if (globalPosition != null)
			graphics.drawText("#$globalPosition / ${userProfile.xp} XP", 4, 39  + shiftY, 244)
		else
			graphics.drawText("${userProfile.xp} XP", 4, 39  + shiftY, 244)

		if (guild != null) {
			val localProfile = ProfileUtils.getLocalProfile(guild, user)

			val localPosition = ProfileUtils.getLocalExperiencePosition(localProfile)

			val xpLocal = localProfile?.xp

			graphics.font = whitneyBold20
			graphics.drawText(guild.name, 4, 61  + shiftY, 244)
			graphics.font = whitneySemiBold20
			if (xpLocal != null) {
				if (localPosition != null) {
					graphics.drawText("#$localPosition / $xpLocal XP", 4, 78 + shiftY, 244)
				} else {
					graphics.drawText("$xpLocal XP", 4, 78 + shiftY, 244)
				}
				graphics.drawText("#${localPosition ?: "???"} / $xpLocal XP", 4, 78 + shiftY, 244)
			} else {
				graphics.drawText("???", 4, 78 + shiftY, 244)
			}
		}

		graphics.font = whitneyBold20
		graphics.drawText("Sonhos", 4, 98  + shiftY, 244)
		graphics.font = whitneySemiBold20
		graphics.drawText(userProfile.money.toString(), 4, 116  + shiftY, 244)

		var x = 272
		var y = 518
		for ((index, badge) in badges.withIndex()) {
			graphics.drawImage(badge.getScaledInstance(29, 29, BufferedImage.SCALE_SMOOTH), x, y, null)
			x += 35

			if (index % 14 == 13) {
				// Aumentar chat box
				val extendedChatBox = readImage(File(Loritta.ASSETS, "profile/msn/extended_chat_box.png"))
				graphics.drawImage(extendedChatBox, 266, y - 38, null)
				x = 272
				y -= 32
			}
		}

		val reputations = ProfileUtils.getReputationCount(user)

		graphics.color = Color.WHITE
		graphics.font = whitneySemiBold20
		graphics.drawText("Reputações: $reputations reps", 11, 73)

		return base
	}
}