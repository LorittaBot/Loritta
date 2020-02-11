package com.mrpowergamerbr.loritta.profile

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO

class DefaultProfileCreator : ProfileCreator {
	override fun create(sender: User, user: User, userProfile: Profile, guild: Guild, serverConfig: MongoServerConfig, badges: List<BufferedImage>, locale: LegacyBaseLocale, background: BufferedImage, aboutMe: String, member: Member?): BufferedImage {
		val profileWrapper = ImageIO.read(File(Loritta.ASSETS, "profile_wrapper_v4.png"))
		val profileWrapperOverlay = ImageIO.read(File(Loritta.ASSETS, "profile_wrapper_v4_overlay.png"))
		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
		val graphics = base.graphics.enableFontAntiAliasing()

		val avatar = LorittaUtils.downloadImage(user.effectiveAvatarUrl)!!.getScaledInstance(115, 115, BufferedImage.SCALE_SMOOTH)

		graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)

		graphics.drawImage(profileWrapper, 0, 0, null)
		graphics.drawImage(avatar.toBufferedImage().makeRoundedCorners(115), 6, 6, null)

		val whitneyMedium = FileInputStream(File(Loritta.ASSETS + "whitney-medium.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
		val whitneySemiBold = FileInputStream(File(Loritta.ASSETS + "whitney-semibold.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
		val whitneyBold = FileInputStream(File(Loritta.ASSETS + "whitney-bold.ttf")).use {
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

		val guildIcon = LorittaUtils.downloadImage(guild.iconUrl?.replace("jpg", "png") ?: "https://emojipedia-us.s3.amazonaws.com/thumbs/320/google/56/shrug_1f937.png")!!.getScaledInstance(38, 38, BufferedImage.SCALE_SMOOTH)

		graphics.font = whitneyBold20
		graphics.drawText("Global", 562, 21, 800 - 6)
		graphics.font = whitneySemiBold20
		val globalPosition = transaction(Databases.loritta) {
			Profiles.select { Profiles.xp greaterEq userProfile.xp }.count()
		}
		graphics.drawText("#$globalPosition / ${userProfile.xp} XP", 562, 39, 800 - 6)

		val localProfile = transaction(Databases.loritta) {
			GuildProfile.find { (GuildProfiles.guildId eq guild.idLong) and (GuildProfiles.userId eq user.idLong) }.firstOrNull()
		}

		val localPosition = if (localProfile != null) {
			transaction(Databases.loritta) {
				GuildProfiles.select { (GuildProfiles.guildId eq guild.idLong) and (GuildProfiles.xp greaterEq localProfile.xp) }.count()
			}
		} else { null }

		val xpLocal = localProfile?.xp

		graphics.font = whitneyBold20
		graphics.drawText(guild.name, 562, 61, 800 - 6)
		graphics.font = whitneySemiBold20
		if (xpLocal != null) {
			graphics.drawText("#$localPosition / $xpLocal XP", 562, 78, 800 - 6)
		} else {
			graphics.drawText("???", 562, 78, 800 - 6)
		}

		val reputations = transaction(Databases.loritta) {
			com.mrpowergamerbr.loritta.tables.Reputations.select { Reputations.receivedById eq user.idLong }.count()
		}

		graphics.font = whitneyBold20
		graphics.drawText("Reputação", 562, 102, 800 - 6)
		graphics.font = whitneySemiBold20
		graphics.drawText("$reputations reps", 562, 120, 800 - 6)

		val globalEconomyPosition = transaction(Databases.loritta) {
			Profiles.select { Profiles.money greaterEq userProfile.money }.count()
		}

		graphics.font = whitneyBold20
		graphics.drawText(locale["ECONOMY_NamePlural"], 562, 492, 800 - 6)
		graphics.font = whitneySemiBold20
		graphics.drawText("#$globalEconomyPosition / ${"%.2f".format(userProfile.money)}", 562, 511, 800 - 6)

		graphics.drawImage(guildIcon.toBufferedImage().makeRoundedCorners(38), 520, 44, null)
		graphics.font = whitneyMedium22

		ImageUtils.drawTextWrapSpaces(aboutMe, 6, 493, 517 - 6, 600, graphics.fontMetrics, graphics)

		graphics.drawImage(profileWrapperOverlay, 0, 0, null)

		return base.makeRoundedCorners(15)
	}
}