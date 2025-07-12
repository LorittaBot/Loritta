package net.perfectdreams.loritta.morenitta.profile.profiles

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImageFromResources
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileGuildInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUtils
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.drawText
import net.perfectdreams.loritta.morenitta.utils.enableFontAntiAliasing
import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage

class OrkutProfileCreator(loritta: LorittaBot) : StaticProfileCreator(loritta, "orkut") {
	override suspend fun create(
		sender: ProfileUserInfoData,
		user: ProfileUserInfoData,
		userProfile: Profile,
		guild: ProfileGuildInfoData?,
		badges: List<BufferedImage>,
		badgesData: List<Badge>,
		equippedBadge: Badge?,
		locale: BaseLocale,
		i18nContext: I18nContext,
		background: BufferedImage,
		aboutMe: String,
		allowedDiscordEmojis: List<Long>?
	): BufferedImage {
		val profileWrapper = readImageFromResources("/profile/orkut/profile_wrapper.png")

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

		val avatar = (LorittaUtils.downloadImage(loritta, user.avatarUrl) ?: Constants.DEFAULT_DISCORD_BLUE_AVATAR).getScaledInstance(200, 200, BufferedImage.SCALE_SMOOTH)

		val latoRegular = 	loritta.graphicsFonts.latoRegular
		val latoBold = 	loritta.graphicsFonts.latoBold
		val latoBlack = 	loritta.graphicsFonts.latoBlack

		val latoBold38 = latoBold.deriveFont(38f)
		val latoRegular32 = latoBold.deriveFont(32f)
		val latoRegular18 = latoRegular.deriveFont(18f)
		val latoBlack20 = latoBlack.deriveFont(20f)
		val latoBold18 = latoBold.deriveFont(18f)
		val latoRegular16 = latoRegular.deriveFont(16f)

		val msnFont20 = latoRegular.deriveFont(20f)
		val msnFont15 = latoBlack20.deriveFont(17f)
		val msnFont24 = latoBlack20.deriveFont(24f)

		graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)
		graphics.drawImage(profileWrapper, 0, 0, null)

		graphics.drawImage(avatar, 8, 8, null)

		graphics.color = Color(102, 102, 102)
		graphics.font = latoBold18
		graphics.drawText(loritta, user.name, 14, 251, 201)
		drawAboutMeWrapSpaces(graphics, graphics.fontMetrics, aboutMe, 256, 51, 773, 1000, allowedDiscordEmojis)

		graphics.color = Color.BLACK
		graphics.drawString("Sua sorte do dia", 267, 257)

		graphics.font = latoRegular18
		graphics.drawString("A Loritta e a Pantufa são suas amigas", 267, 276)

		val reputations = ProfileUtils.getReputationCount(loritta, user)
		val reversedRep = reputations.toString().reversed()

		var startX = 202
		graphics.color = Color(153, 153, 153)
		for (ch in reversedRep) {
			startX -= graphics.fontMetrics.charWidth(ch)
			graphics.drawString(ch.toString(), startX, 291)
		}

		drawBadges(badges, graphics)

		/* val mutualGuildsByUsers = loritta.lorittaShards.getMutualGuilds(user).filter { it.iconUrl != null }.sortedByDescending { it.members.size }

		var startGuildX = 252
		var startGuildY = 400

		graphics.color = Color(42, 127, 212)
		graphics.font = latoRegular16
		for ((index, guild) in mutualGuildsByUsers.withIndex()) {
			if (index == 8)
				break
			if (index == 4) {
				startGuildX = 252
				startGuildY += 113 + 26
			}

			if (guild.iconUrl != null) {
				val icon = LorittaUtils.downloadImage(loritta, guild.iconUrl!!)!!.getScaledInstance(113, 113, BufferedImage.SCALE_SMOOTH)
				graphics.drawImage(icon, startGuildX, startGuildY, null)
				graphics.drawText(loritta, guild.name, startGuildX, startGuildY + 113 + 18, startGuildX + 113)
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