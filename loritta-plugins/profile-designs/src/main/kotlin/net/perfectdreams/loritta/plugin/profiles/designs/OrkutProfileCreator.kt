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
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream

class OrkutProfileCreator : ProfileCreator("orkut") {
	override suspend fun create(sender: ProfileUserInfoData, user: ProfileUserInfoData, userProfile: Profile, guild: Guild?, badges: List<BufferedImage>, locale: BaseLocale, background: BufferedImage, aboutMe: String): BufferedImage {
		val profileWrapper = readImage(File(Loritta.ASSETS, "profile/orkut/profile_wrapper.png"))

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

		val avatar = LorittaUtils.downloadImage(user.avatarUrl)!!.getScaledInstance(200, 200, BufferedImage.SCALE_SMOOTH)

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

		val reputations = ProfileUtils.getReputationCount(user)
		val reversedRep = reputations.toString().reversed()

		var startX = 202
		graphics.color = Color(153, 153, 153)
		for (ch in reversedRep) {
			startX -= graphics.fontMetrics.charWidth(ch)
			graphics.drawString(ch.toString(), startX, 291)
		}

		drawBadges(badges, graphics)

		/* val mutualGuildsByUsers = lorittaShards.getMutualGuilds(user).filter { it.iconUrl != null }.sortedByDescending { it.members.size }

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

			if (guild.iconUrl != null) {
				val icon = LorittaUtils.downloadImage(guild.iconUrl!!)!!.getScaledInstance(113, 113, BufferedImage.SCALE_SMOOTH)
				graphics.drawImage(icon, startGuildX, startGuildY, null)
				graphics.drawText(guild.name, startGuildX, startGuildY + 113 + 18, startGuildX + 113)
				startGuildX += 113 + 12
			}
		}

		graphics.color = Color(51, 51, 93)
		graphics.drawString("Comunidades (${mutualGuildsByUsers.size})", 226, 332) */
		return base
	}

	fun drawBadges(badges: List<BufferedImage>, graphics: Graphics) {
		var startGuildX = 235
		var startGuildY = 400

		for ((index, badge) in badges.withIndex()) {
			if (index == 27)
				break

			if (index == 9) {
				startGuildX = 235
				startGuildY += 57
			}

			graphics.drawImage(badge.getScaledInstance(56, 56, BufferedImage.SCALE_SMOOTH), startGuildX, startGuildY, null)

			startGuildX += 57
		}
	}
}