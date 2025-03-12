package net.perfectdreams.loritta.morenitta.profile.profiles

import mu.KotlinLogging
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImageFromResources
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.GuildProfiles
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.GuildProfile
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileGuildInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.profile.ProfileUtils
import net.perfectdreams.loritta.morenitta.utils.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.awt.Color
import java.awt.Rectangle
import java.awt.image.BufferedImage

open class NostalgiaProfileCreator(loritta: LorittaBot, internalName: String, val folderName: String) : StaticProfileCreator(loritta, internalName) {
	class NostalgiaDarkProfileCreator(loritta: LorittaBot) : NostalgiaProfileCreator(loritta, "defaultDark", "dark")
	class NostalgiaBlurpleProfileCreator(loritta: LorittaBot) : NostalgiaProfileCreator(loritta, "defaultBlurple", "blurple")
	class NostalgiaRedProfileCreator(loritta: LorittaBot) : NostalgiaProfileCreator(loritta, "defaultRed", "red")
	class NostalgiaBlueProfileCreator(loritta: LorittaBot) : NostalgiaProfileCreator(loritta, "defaultBlue", "blue")
	class NostalgiaGreenProfileCreator(loritta: LorittaBot) : NostalgiaProfileCreator(loritta, "defaultGreen", "green")
	class NostalgiaPurpleProfileCreator(loritta: LorittaBot) : NostalgiaProfileCreator(loritta, "defaultPurple", "purple")
	class NostalgiaPinkProfileCreator(loritta: LorittaBot) : NostalgiaProfileCreator(loritta, "defaultPink", "pink")
	class NostalgiaYellowProfileCreator(loritta: LorittaBot) : NostalgiaProfileCreator(loritta, "defaultYellow", "yellow")
	class NostalgiaOrangeProfileCreator(loritta: LorittaBot) : NostalgiaProfileCreator(loritta, "defaultOrange", "orange")
	class NostalgiaEaster2023ProfileCreator(loritta: LorittaBot) : NostalgiaProfileCreator(loritta, "defaultEaster2023", "easter2023")

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
		val profileWrapper = readImageFromResources("/profile/nostalgia/profile_wrapper_$folderName.png")

		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

		val avatar = LorittaUtils.downloadImage(loritta, user.avatarUrl)!!.getScaledInstance(152, 152, BufferedImage.SCALE_SMOOTH)

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
			Reputations.selectAll().where { Reputations.receivedById eq user.id.toLong() }.count()
		}

		graphics.color = Color.WHITE
		graphics.font = oswaldRegular50
		graphics.drawText(loritta, user.name, 159, 46) // Nome do usuário
		graphics.font = oswaldRegular42
		ImageUtils.drawCenteredString(graphics, "${reputations} reps", Rectangle(598, 54, 202, 54), oswaldRegular42)
		graphics.font = oswaldRegular29
		ImageUtils.drawCenteredString(graphics, locale["profile.aboutMe"], Rectangle(0, 465, 132, 38), oswaldRegular29)

		var x = 162
		for (badge in badges) {
			graphics.drawImage(badge.getScaledInstance(34, 34, BufferedImage.SCALE_SMOOTH), x, 466, null)
			x += 42
		}

		val latoRegular = loritta.graphicsFonts.latoRegular
		val latoBold = loritta.graphicsFonts.latoBold
		val latoBlack = loritta.graphicsFonts.latoBlack

		val latoBold38 = latoBold.deriveFont(38f)
		val latoRegular22 = latoBold.deriveFont(22f)
		val latoBlack20 = latoBlack.deriveFont(20f)
		val latoBold20 = latoBold.deriveFont(20f)

		graphics.font = latoRegular22

		drawAboutMeWrapSpaces(graphics, graphics.fontMetrics, aboutMe, 6, 522, 800 - 6, 600, allowedDiscordEmojis)

		val shiftY = 42

		graphics.font = latoBlack20
		graphics.drawText(loritta, "Global", 159, 21 + shiftY, 800 - 6)
		graphics.font = latoBold20
		graphics.drawText(loritta, "${userProfile.xp} XP", 159, 39  + shiftY, 800 - 6)

		if (guild != null) {
			val localProfile = loritta.newSuspendedTransaction {
				GuildProfile.find { (GuildProfiles.guildId eq guild.id.toLong()) and (GuildProfiles.userId eq user.id.toLong()) }.firstOrNull()
			}

			val localPosition = ProfileUtils.getLocalExperiencePosition(loritta, localProfile)

			val xpLocal = localProfile?.xp

			graphics.font = latoBlack20
			graphics.drawText(loritta, guild.name, 159, 61 + shiftY, 800 - 6)
			graphics.font = latoBold20
			if (xpLocal != null) {
				if (localPosition != null)
					graphics.drawText(loritta, "#$localPosition / $xpLocal XP", 159, 78 + shiftY, 800 - 6)
				else
					graphics.drawText(loritta, "$xpLocal XP", 159, 78 + shiftY, 800 - 6)
			} else {
				graphics.drawText(loritta, "???", 159, 78 + shiftY, 800 - 6)
			}
		}

		val globalEconomyPosition = ProfileUtils.getGlobalEconomyPosition(loritta, userProfile)

		graphics.font = latoBlack20
		graphics.drawText(loritta, "Sonhos", 159, 98  + shiftY, 800 - 6)
		graphics.font = latoBold20
		if (globalEconomyPosition != null)
			graphics.drawText(loritta, "#$globalEconomyPosition / ${userProfile.money}", 159, 116  + shiftY, 800 - 6)
		else
			graphics.drawText(loritta, "${userProfile.money}", 159, 116  + shiftY, 800 - 6)

		val marriage = loritta.newSuspendedTransaction { userProfile.marriage }

		if (marriage != null) {
			val marriedWithId = if (marriage.user1 == user.id) {
				marriage.user2
			} else {
				marriage.user1
			}.toString()

			val marrySection = readImageFromResources("/profile/nostalgia/marry.png")
			graphics.drawImage(marrySection, 0, 0, null)
			KotlinLogging.logger {}.info { "NostalgiaProfileCreator#retrieveUserInfoById - UserId: ${marriedWithId}" }
			val marriedWith = loritta.lorittaShards.retrieveUserInfoById(marriedWithId.toLong())

			if (marriedWith != null) {
				val latoBold16 = latoBold.deriveFont(16f)
				val latoRegular20 = latoRegular22.deriveFont(20f)
				graphics.font = latoBold16
				ImageUtils.drawCenteredString(graphics, locale["profile.marriedWith"], Rectangle(545, 108, 256, 14), latoBold16)
				graphics.font = latoRegular20
				ImageUtils.drawCenteredString(graphics, marriedWith.name, Rectangle(545, 108 + 14, 256, 18), latoRegular20)
				graphics.font = latoBold16
				ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(i18nContext, marriage.marriedSince, System.currentTimeMillis(), 3), Rectangle(545, 108 + 14  + 18, 256, 14), latoBold16)
			}
		}

		return base
	}
}