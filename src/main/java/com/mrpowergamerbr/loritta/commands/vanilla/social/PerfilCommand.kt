package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.userdata.LorittaServerUserData
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.makeRoundedCorners
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class PerfilCommand : com.mrpowergamerbr.loritta.commands.CommandBase() {
	override fun getLabel(): String {
		return "perfil";
	}

	override fun getAliases(): MutableList<String> {
		return Arrays.asList("profile");
	}

	override fun getDescription(): String {
		return "Mostra o seu perfil!";
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.SOCIAL;
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: com.mrpowergamerbr.loritta.commands.CommandContext) {
		var userData = context.config.userData.getOrDefault(context.userHandle.id, LorittaServerUserData());
		var base = BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB); // Base
		val graphics = base.graphics as java.awt.Graphics2D;
		graphics.setRenderingHint(
				java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
				java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		var userProfile = context.lorittaUser.profile

		var user = if (context.message.mentionedUsers.size == 1) context.message.mentionedUsers[0] else context.userHandle
		if (user == null) {
			context.sendMessage(context.getAsMention(true) + "Não foi encontrado nenhum usuário com este nome!");
			return;
		}

		if (context.message.mentionedUsers.size == 1) {
			userProfile = loritta.getLorittaProfileForUser(context.message.mentionedUsers[0].id)
			userData = context.config.userData.getOrDefault(context.message.mentionedUsers[0].id, LorittaServerUserData());
		}

		val profileWrapper = ImageIO.read(File(Loritta.FOLDER, "profile_wrapper_v2.png"))

		var file = File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + userProfile.userId + ".png");

		val background = when {
			file.exists() -> ImageIO.read(File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + userProfile.userId + ".png")) // Background padrão
			else -> ImageIO.read(File(Loritta.FOLDER + "default_background.png")) // Background padrão
		}

		val avatar = LorittaUtils.downloadImage(user.effectiveAvatarUrl).getScaledInstance(72, 72, BufferedImage.SCALE_SMOOTH)
		val fullBar = ImageIO.read(File(Loritta.FOLDER + "profile_wrapper_v2_full.png"))
		val emptyBar = ImageIO.read(File(Loritta.FOLDER + "profile_wrapper_v2_empty.png"))

		graphics.drawImage(background, 0, 0, null); // Background fica atrás de tudo

		graphics.drawImage(profileWrapper, 0, 0, null)

		graphics.drawImage(avatar, 4, 4, null);

		val guildImages = ArrayList<java.awt.Image>();

		val guilds = lorittaShards.getGuilds()
				.filter { guild -> guild.isMember(user) }
				.sortedByDescending { it.members.size }

		var idx = 0;
		for (guild in guilds) {
			if (guild.iconUrl != null) {
				if (idx > 16) {
					break;
				}
				try {
					val connection = java.net.URL(guild.iconUrl).openConnection() as java.net.HttpURLConnection
					connection.setRequestProperty(
							"User-Agent",
							"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0")
					var guild = javax.imageio.ImageIO.read(connection.inputStream)
					var guildImg = guild.getScaledInstance(18, 18, java.awt.Image.SCALE_SMOOTH).toBufferedImage()
					guildImg = guildImg.getSubimage(1, 1, guildImg.height - 1, guildImg.width - 1)
					guildImg = guildImg.makeRoundedCorners(999)
					guildImages.add(guildImg)
					idx++;
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
			val minecraftia = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
					java.io.FileInputStream(java.io.File(com.mrpowergamerbr.loritta.Loritta.FOLDER + "minecraftia.ttf")))

			graphics.font = minecraftia.deriveFont(8F);

			val textToBeDrawn = "+" + (guilds.size - 14) + " guilds"

			graphics.color = Color(0, 0, 0)
			val textSize = graphics.fontMetrics.stringWidth(textToBeDrawn)
			graphics.drawString("+" + (guilds.size - 14) + " guilds", 398 - textSize, 227)
			graphics.drawString("+" + (guilds.size - 14) + " guilds", 396 - textSize, 227)
			graphics.drawString("+" + (guilds.size - 14) + " guilds", 397 - textSize, 228)
			graphics.drawString("+" + (guilds.size - 14) + " guilds", 397 - textSize, 226)

			graphics.color = Color(255, 255, 255)
			graphics.drawString("+" + (guilds.size - 14) + " guilds", 397 - graphics.fontMetrics.stringWidth(textToBeDrawn), 227)
		}

		// Escrever o "Sobre Mim"
		val bariolRegular = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
				java.io.FileInputStream(java.io.File(com.mrpowergamerbr.loritta.Loritta.FOLDER + "bariol_regular.otf")))

		graphics.font = bariolRegular.deriveFont(13F)

		var aboutMe = if (Loritta.config.clientId == userProfile.userId) {
			"Olá, eu me chamo Loritta (ou, como meus amigos próximos me chamam, \"Lori\") e sou apenas um simples bot brasileiro para o Discord com várias funções jamais vistas!"
		} else {
			userProfile.aboutMe
		}

		graphics.color = Color(128, 128, 128, 128)
		ImageUtils.drawTextWrapSpaces(aboutMe, 2, 253 + graphics.fontMetrics.descent + 8, 400, 9999, graphics.fontMetrics, graphics)
		graphics.color = Color(255, 255, 255)
		ImageUtils.drawTextWrapSpaces(aboutMe, 2, 253 + graphics.fontMetrics.descent + 7, 400, 9999, graphics.fontMetrics, graphics)

		// Informações sobre o usuário
		graphics.font = bariolRegular.deriveFont(11F)

		graphics.drawString("XP Total", 80, 39)
		graphics.drawString(if (Loritta.config.clientId == userProfile.userId) ":)" else userProfile.xp.toString(), 220, 39)
		ImageUtils.drawTextWrap("XP no ${context.guild.name}", 80, 55, 9999, 9999, graphics.fontMetrics, graphics)
		graphics.drawString(if (Loritta.config.clientId == userProfile.userId) ";)" else userData.xp.toString(), 220, 55)
		graphics.drawString("Sonhos", 80, 71)
		graphics.drawString(if (Loritta.config.clientId == userProfile.userId) "^-^" else "0", 220, 71)
		// Escrever nome do usuário
		val oswaldRegular = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
				java.io.FileInputStream(java.io.File(com.mrpowergamerbr.loritta.Loritta.FOLDER + "oswald_regular.ttf")))
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

		ImageUtils.drawCenteredString(graphics, "Sobre Mim", Rectangle(0, 232, 61, 19), oswaldRegular11)

		// Barrinha de XP
		graphics.drawImage(emptyBar, 0, 0, null)

		val xpWrapper = userData.getCurrentLevel()
		val nextLevel = userData.getExpToAdvanceFrom(userData.getCurrentLevel().currentLevel + 1)
		val currentLevel = xpWrapper.expLeft
		val percentage = (currentLevel.toDouble() / nextLevel.toDouble());

		if ((400 * percentage).toInt() >= 1) {
			graphics.drawImage(fullBar.getSubimage(0, 0, (400 * percentage).toInt(), 300), 0, 0, null)
		}

		graphics.color = Color(0, 111, 84, 190)
		val bariol11 = bariolRegular.deriveFont(11F)
		graphics.font = bariol11
		ImageUtils.drawCenteredString(graphics, "$currentLevel/$nextLevel XP", Rectangle(0, 83, 66, 13), bariol11)

		context.sendFile(base.makeRoundedCorners(15), "profile.png", "📝 **|** " + context.getAsMention(true) + "Perfil"); // E agora envie o arquivo
	}

	fun drawWithShadow(text: String, x: Int, y: Int, maxX: Int, maxY: Int, graphics: Graphics) {
		graphics.color = java.awt.Color(75, 75, 75, 75);
		ImageUtils.drawTextWrapSpaces(text, x, y + 1, maxX, maxY, graphics.fontMetrics, graphics);
		graphics.color = java.awt.Color(118, 118, 118);
		ImageUtils.drawTextWrapSpaces(text, x, y, maxX, maxY, graphics.fontMetrics, graphics);
	}

	data class GamePlayed(val game: String, val timeSpent: Long)
}