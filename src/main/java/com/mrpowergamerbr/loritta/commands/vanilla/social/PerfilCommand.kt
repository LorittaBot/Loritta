package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.string
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.JSON_PARSER
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.artist
import com.mrpowergamerbr.loritta.utils.donator
import com.mrpowergamerbr.loritta.utils.drawText
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.makeRoundedCorners
import com.mrpowergamerbr.loritta.utils.patreon
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class PerfilCommand : AbstractCommand("perfil", listOf("profile"), CommandCategory.SOCIAL) {
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
		var userData = context.config.getUserData(context.userHandle.id)
		var base = BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB); // Base
		val graphics = base.graphics as java.awt.Graphics2D;
		graphics.setRenderingHint(
				java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
				java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		var userProfile = context.lorittaUser.profile

		var contextUser = LorittaUtils.getUserFromContext(context, 0)
		var user = if (contextUser != null) contextUser else context.userHandle

		if (contextUser != null) {
			userProfile = loritta.getLorittaProfileForUser(contextUser.id)
			userData = context.config.getUserData(contextUser.id);
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

		val profileWrapper = ImageIO.read(File(Loritta.ASSETS, "profile_wrapper_v2.png"))

		var file = File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + userProfile.userId + ".png");

		val background = when {
			file.exists() -> ImageIO.read(File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + userProfile.userId + ".png")) // Background padrão
			else -> ImageIO.read(File(Loritta.ASSETS + "default_background.png")) // Background padrão
		}

		val avatar = LorittaUtils.downloadImage(user.effectiveAvatarUrl).getScaledInstance(72, 72, BufferedImage.SCALE_SMOOTH)
		val fullBar = ImageIO.read(File(Loritta.ASSETS + "profile_wrapper_v2_full.png"))
		val emptyBar = ImageIO.read(File(Loritta.ASSETS + "profile_wrapper_v2_empty.png"))
		val fullGlobalBar = ImageIO.read(File(Loritta.ASSETS + "profile_wrapper_v2_globalfull.png"))
		val emptyGlobalBar = ImageIO.read(File(Loritta.ASSETS + "profile_wrapper_v2_globalempty.png"))

		graphics.drawImage(background, 0, 0, null); // Background fica atrás de tudo

		graphics.drawImage(profileWrapper, 0, 0, null)

		// Draw Avatar
		graphics.drawImage(avatar.toBufferedImage().makeRoundedCorners(72), 4, 4, null)

		// biscord bots
		val discordBotsResponse = HttpRequest.get("https://discordbots.org/api/bots/${Loritta.config.clientId}/votes?onlyids=1")
				.authorization(Loritta.config.discordBotsOrgKey)
				.body()

		println(discordBotsResponse)

		val idArray = JSON_PARSER.parse(discordBotsResponse).array

		val upvotedOnDiscordBots = idArray.any { it.string == user.id }
		val badge = when {
			user.patreon || user.id == Loritta.config.ownerId -> ImageIO.read(File(Loritta.ASSETS + "blob_blush.png"))
			user.donator -> ImageIO.read(File(Loritta.ASSETS + "blob_blush2.png"))
			user.artist -> ImageIO.read(File(Loritta.ASSETS + "artist_badge.png"))
			user.id == Loritta.config.clientId -> ImageIO.read(File(Loritta.ASSETS + "loritta_badge.png"))
			user.isBot -> ImageIO.read(File(Loritta.ASSETS + "robot_badge.png"))
			upvotedOnDiscordBots -> ImageIO.read(File(Loritta.ASSETS + "upvoted_badge.png"))
			else -> null
		}

		if (badge != null) {
			graphics.drawImage(badge.getScaledInstance(27, 27, BufferedImage.SCALE_SMOOTH), 52, 52, null)
		}

		val guildImages = ArrayList<java.awt.Image>();

		val guilds = lorittaShards.getMutualGuilds(user)
				.sortedByDescending { it.members.size }

		var idx = 0;
		for (guild in guilds) {
			if (guild.iconUrl != null) {
				if (idx > 16) {
					break;
				}
				try {
					var guild = LorittaUtils.downloadImage(guild.iconUrl)
					var guildImg = guild.getScaledInstance(18, 18, java.awt.Image.SCALE_SMOOTH).toBufferedImage()
					guildImg = guildImg.getSubimage(1, 1, guildImg.height - 1, guildImg.width - 1)
					guildImg = guildImg.makeRoundedCorners(999)
					guildImages.add(guildImg)
					idx++
				} catch (e: Exception) {
				}
			}
		}

		var guildX = 81;
		var guildY = 233;
		for (guild in guildImages) {
			graphics.drawImage(guild, guildX, guildY, null);
			guildX += 19;
		}

		if (idx > 16) {
			val minecraftia = Constants.MINECRAFTIA

			graphics.font = minecraftia.deriveFont(8F)

			val textToBeDrawn = "+" + (guilds.size - 16) + " guilds"

			graphics.color = Color(0, 0, 0)
			val textSize = graphics.fontMetrics.stringWidth(textToBeDrawn)
			graphics.drawString(textToBeDrawn, 398 - textSize, 227 + 5)
			graphics.drawString(textToBeDrawn, 396 - textSize, 227 + 5)
			graphics.drawString(textToBeDrawn, 397 - textSize, 228 + 5)
			graphics.drawString(textToBeDrawn, 397 - textSize, 226 + 5)

			graphics.color = Color(255, 255, 255)
			graphics.drawString(textToBeDrawn, 397 - graphics.fontMetrics.stringWidth(textToBeDrawn), 227 + 5)
		}

		// Escrever o "Sobre Mim"
		val bariolRegular = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
				java.io.FileInputStream(java.io.File(com.mrpowergamerbr.loritta.Loritta.ASSETS + "bariol_regular.otf")))

		graphics.font = bariolRegular.deriveFont(13F)

		var aboutMe = if (Loritta.config.clientId == userProfile.userId) {
			context.locale["PERFIL_LORITTA_DESCRIPTION"]
		} else {
			userProfile.aboutMe
		}

		graphics.color = Color(128, 128, 128, 128)
		ImageUtils.drawTextWrapSpaces(aboutMe, 2, 253 + graphics.fontMetrics.descent + 8, 400, 9999, graphics.fontMetrics, graphics)
		graphics.color = Color(255, 255, 255)
		ImageUtils.drawTextWrapSpaces(aboutMe, 2, 253 + graphics.fontMetrics.descent + 7, 400, 9999, graphics.fontMetrics, graphics)

		// Informações sobre o usuário
		graphics.font = bariolRegular.deriveFont(11F)

		graphics.drawString(context.locale["PERFIL_TOTAL_XP"], 80, 39)
		graphics.drawString(if (Loritta.config.clientId == userProfile.userId) ":)" else userProfile.xp.toString(), 220, 39)
		graphics.drawText(context.locale["PERFIL_XP_GUILD", context.guild.name], 80, 55, 216)
		graphics.drawString(if (Loritta.config.clientId == userProfile.userId) ";)" else userData.xp.toString(), 220, 55)
		graphics.drawString(context.locale["PERFIL_ECONOMY"], 80, 71)
		graphics.drawString(if (Loritta.config.clientId == userProfile.userId) "^-^" else userProfile.dreams.toString(), 220, 71)
		// Escrever nome do usuário
		val oswaldRegular = Constants.OSWALD_REGULAR
				.deriveFont(23F)

		graphics.font = oswaldRegular

		graphics.color = Color(128, 128, 128, 128)
		graphics.drawString(user.name, 82, 22)
		graphics.color = Color(255, 255, 255)
		graphics.drawString(user.name, 82, 22)

		val oswaldRegular20 = oswaldRegular.deriveFont(20F)

		graphics.font = oswaldRegular20

		// Reputação do Usuário
		ImageUtils.drawCenteredString(graphics, "${userProfile.getReputation()} rep", Rectangle(299, 27, 101, 29), oswaldRegular20)

		// Título do "Sobre Mim"
		val oswaldRegular11 = oswaldRegular.deriveFont(11F)

		graphics.font = oswaldRegular11

		ImageUtils.drawCenteredString(graphics, context.locale["PERFIL_SOBRE_MIM"], Rectangle(0, 232, 61, 19), oswaldRegular11)

		// Barrinha de XP
		run {
			graphics.drawImage(emptyBar, 0, 0, null)

			val xpWrapper = userData.getCurrentLevel()
			val nextLevel = userData.getExpToAdvanceFrom(xpWrapper.currentLevel + 1)
			val currentLevel = xpWrapper.expLeft
			val percentage = (currentLevel.toDouble() / nextLevel.toDouble());

			if ((400 * percentage).toInt() >= 1) {
				graphics.drawImage(fullBar.getSubimage(0, 0, (400 * percentage).toInt(), 300), 0, 0, null)
			}

			graphics.color = Color(0, 111, 84, 190)
			val bariol11 = bariolRegular.deriveFont(11F)
			graphics.font = bariol11
			ImageUtils.drawCenteredString(graphics, "$currentLevel/$nextLevel XP", Rectangle(0, 83, 66, 13), bariol11)
			ImageUtils.drawCenteredString(graphics, "lvl ${xpWrapper.currentLevel}", Rectangle(67, 83, 47, 13), bariol11)
		}

		// Barrinha de XP Global
		run {
			graphics.drawImage(emptyGlobalBar, 0, 0, null)

			val lorittaProfile = loritta.getLorittaProfileForUser(user.id)
			val xpWrapper = lorittaProfile.getCurrentLevel()
			val nextLevel = lorittaProfile.getExpToAdvanceFrom(xpWrapper.currentLevel + 1)
			val currentLevel = xpWrapper.expLeft
			val percentage = (currentLevel.toDouble() / nextLevel.toDouble());

			if ((400 * percentage).toInt() >= 1) {
				graphics.drawImage(fullGlobalBar.getSubimage(0, 0, (400 * percentage).toInt(), 300), 0, 0, null)
			}

			graphics.color = Color(131, 23, 183, 190)
			val bariol11 = bariolRegular.deriveFont(11F)
			graphics.font = bariol11
			ImageUtils.drawCenteredString(graphics, "$currentLevel/$nextLevel XP", Rectangle(0, 216, 66, 13), bariol11)
			ImageUtils.drawCenteredString(graphics, "lvl ${xpWrapper.currentLevel}", Rectangle(67, 216, 47, 13), bariol11)
		}

		context.sendFile(base.makeRoundedCorners(15), "profile.png", "📝 **|** " + context.getAsMention(true) + context.locale["PEFIL_PROFILE"]); // E agora envie o arquivo
	}

	fun drawWithShadow(text: String, x: Int, y: Int, maxX: Int, maxY: Int, graphics: Graphics) {
		graphics.color = java.awt.Color(75, 75, 75, 75);
		ImageUtils.drawTextWrapSpaces(text, x, y + 1, maxX, maxY, graphics.fontMetrics, graphics);
		graphics.color = java.awt.Color(118, 118, 118);
		ImageUtils.drawTextWrapSpaces(text, x, y, maxX, maxY, graphics.fontMetrics, graphics);
	}

	data class GamePlayed(val game: String, val timeSpent: Long)
}