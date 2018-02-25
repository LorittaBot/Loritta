package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonArray
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.util.*
import javax.imageio.ImageIO

class PerfilCommand : AbstractCommand("perfil", listOf("profile"), CommandCategory.SOCIAL) {
	companion object {
		var ID_ARRAY: JsonArray? = null
		var lastQuery = 0L
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["PERFIL_DESCRIPTION"]
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		var userProfile = context.lorittaUser.profile

		var contextUser = LorittaUtils.getUserFromContext(context, 0)
		var user = if (contextUser != null) contextUser else context.userHandle

		if (contextUser != null) {
			userProfile = loritta.getLorittaProfileForUser(contextUser.id)
		}

		if (userProfile.isBanned) {
			context.reply(
					LoriReply(
							"${contextUser.asMention} está **banido**",
							"\uD83D\uDE45"
					),
					LoriReply(
							"**Motivo:** `${userProfile.banReason}`",
							"✍"
					)
			)
			return
		}
		// Para pegar o "Jogando" do usuário, nós precisamos pegar uma guild que o usuário está
		var member = lorittaShards.getMutualGuilds(user).firstOrNull()?.getMember(user)

		try {
			// biscord bots
			if (System.currentTimeMillis() - lastQuery > 60000) {
				val discordBotsResponse = HttpRequest.get("https://discordbots.org/api/bots/${Loritta.config.clientId}/votes?onlyids=1")
						.authorization(Loritta.config.discordBotsOrgKey)
						.body()

				lastQuery = System.currentTimeMillis()
				ID_ARRAY = JSON_PARSER.parse(discordBotsResponse).array
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}


		var upvotedOnDiscordBots = if (ID_ARRAY != null) {
			ID_ARRAY!!.any { it.string == user.id }
		} else {
			false
		}

		val lorittaGuild = lorittaShards.getGuildById("297732013006389252")
		var hasNotifyMeRole = if (lorittaGuild != null) {
			if (lorittaGuild.isMember(user)) {
				val member = lorittaGuild.getMember(user)
				val role = lorittaGuild.getRoleById("334734175531696128")
				member.roles.contains(role)
			} else {
				false
			}
		} else {
			false
		}
		var usesPocketDreamsRichPresence = if (member != null) {
			val game = member.game
			if (game != null && game.isRich) {
				game.asRichPresence().applicationId == "415617983411388428"
			} else {
				false
			}
		} else {
			false
		}
		val pocketDreamsGuild = lorittaShards.getGuildById("320248230917046282")
		var isPocketDreamsStaff = if (pocketDreamsGuild != null) {
			if (pocketDreamsGuild.isMember(user)) {
				val member = pocketDreamsGuild.getMember(user)
				val role = pocketDreamsGuild.getRoleById("332650495522897920")
				member.roles.contains(role)
			} else {
				false
			}
		} else {
			false
		}

		val badges = mutableListOf<BufferedImage>()
		if (user.patreon || user.id == Loritta.config.ownerId) badges += ImageIO.read(File(Loritta.ASSETS + "blob_blush.png"))
		if (user.supervisor) badges += ImageIO.read(File(Loritta.ASSETS + "supervisor.png"))
		if (isPocketDreamsStaff) badges += ImageIO.read(File(Loritta.ASSETS + "pocketdreams_staff.png"))
		if (user.support) badges += ImageIO.read(File(Loritta.ASSETS + "support.png"))
		if (user.donator) badges += ImageIO.read(File(Loritta.ASSETS + "blob_blush2.png"))
		if (user.artist) badges += ImageIO.read(File(Loritta.ASSETS + "artist_badge.png"))
		if (hasNotifyMeRole) badges += ImageIO.read(File(Loritta.ASSETS + "notify_me.png"))
		if (usesPocketDreamsRichPresence) badges += ImageIO.read(File(Loritta.ASSETS + "pocketdreams_rp.png"))
		if (user.id == Loritta.config.clientId) badges += ImageIO.read(File(Loritta.ASSETS + "loritta_badge.png"))
		if (user.isBot) badges += ImageIO.read(File(Loritta.ASSETS + "robot_badge.png"))
		if (upvotedOnDiscordBots) badges += ImageIO.read(File(Loritta.ASSETS + "upvoted_badge.png"))

		val profileWrapper = ImageIO.read(File(Loritta.ASSETS, "profile_wrapper_v4.png"))
		val profileWrapperOverlay = ImageIO.read(File(Loritta.ASSETS, "profile_wrapper_v4_overlay.png"))
		val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB); // Base
		val graphics = base.graphics as java.awt.Graphics2D;
		graphics.setRenderingHint(
				java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
				java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		val avatar = LorittaUtils.downloadImage(user.effectiveAvatarUrl).getScaledInstance(115, 115, BufferedImage.SCALE_SMOOTH)

		val file = File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + userProfile.userId + ".png")

		val background = when {
			file.exists() -> ImageIO.read(File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + userProfile.userId + ".png")) // Background padrão
			else -> ImageIO.read(File(Loritta.ASSETS + "default_background.png")) // Background padrão
		}

		graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null) // TODO: Permitir backgrounds maiores
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

		val guildIcon = LorittaUtils.downloadImage(context.guild?.iconUrl?.replace("jpg", "png") ?: "https://emojipedia-us.s3.amazonaws.com/thumbs/320/google/56/shrug_1f937.png").getScaledInstance(38, 38, BufferedImage.SCALE_SMOOTH)

		graphics.font = whitneyBold20
		graphics.drawText("Global", 562, 21, 800 - 6)
		graphics.font = whitneySemiBold20
		graphics.drawText("#$position / ${userProfile.xp} XP", 562, 39, 800 - 6)

		val localPosition = context.config.guildUserData.sortedByDescending { it.xp }.indexOfFirst { it.userId == userProfile.userId } + 1
		val xpLocal = context.config.guildUserData.firstOrNull { it.userId == userProfile.userId }

		graphics.font = whitneyBold20
		graphics.drawText(context.guild.name, 562, 61, 800 - 6)
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
		graphics.drawText(context.locale["PERFIL_ECONOMY"], 562, 492, 800 - 6)
		graphics.font = whitneySemiBold20
		graphics.drawText("${context.lorittaUser.profile.dreams}", 562, 511, 800 - 6)

		graphics.drawImage(guildIcon.toBufferedImage().makeRoundedCorners(38), 520, 44, null)
		graphics.font = whitneyMedium22

		ImageUtils.drawTextWrapSpaces(userProfile.aboutMe, 6, 493, 517 - 6, 600, graphics.fontMetrics, graphics)

		graphics.drawImage(profileWrapperOverlay, 0, 0, null)

		context.sendFile(base.makeRoundedCorners(15), "lori_profile.png", "📝 **|** " + context.getAsMention(true) + context.locale["PEFIL_PROFILE"]); // E agora envie o arquivo
	}
}