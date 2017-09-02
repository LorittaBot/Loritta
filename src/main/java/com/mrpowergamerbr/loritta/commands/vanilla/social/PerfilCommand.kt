package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.userdata.LorittaServerUserData
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.makeRoundedCorners
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
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

        if (context.guild.id == "268353819409252352") {
			val user = context.userHandle

            val profileWrapper = ImageIO.read(File(Loritta.FOLDER, "profile_wrapper_v2.png"))

			var background: BufferedImage?

			var file = File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + userProfile.userId + ".png");

			background = when {
				file.exists() -> ImageIO.read(File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + userProfile.userId + ".png")) // Background padrão
				else -> ImageIO.read(File(Loritta.FOLDER + "default_background.png")) // Background padrão
			}

            val avatar = LorittaUtils.downloadImage(user.effectiveAvatarUrl).getScaledInstance(72, 72, BufferedImage.SCALE_SMOOTH)

			graphics.drawImage(background, 0, 0, null); // Background fica atrás de tudo

			graphics.drawImage(profileWrapper, 0, 0, null)

			graphics.drawImage(avatar, 4, 4, null);

			val guildImages = ArrayList<java.awt.Image>();

			val guilds = LorittaLauncher.getInstance().lorittaShards.getGuilds()
					.filter { guild -> guild.isMember(user) }
					.sortedByDescending { it.members.size }

			var idx = 0;
			for (guild in guilds) {
				if (guild.iconUrl != null) {
					if (idx > 20) {
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

			// Escrever o "Sobre Mim"
			val bariolRegular = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
					java.io.FileInputStream(java.io.File(com.mrpowergamerbr.loritta.Loritta.FOLDER + "bariol_regular.otf")))

			graphics.font = bariolRegular.deriveFont(13F)

			graphics.color = Color(128, 128, 128, 128)
			ImageUtils.drawTextWrapSpaces(userProfile.aboutMe, 2, 253 + graphics.fontMetrics.descent + 8, 400, 9999, graphics.fontMetrics, graphics)
			graphics.color = Color(255, 255, 255)
			ImageUtils.drawTextWrapSpaces(userProfile.aboutMe, 2, 253 + graphics.fontMetrics.descent + 7, 400, 9999, graphics.fontMetrics, graphics)

			// Escrever nome do usuário
			val oswaldRegular = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
					java.io.FileInputStream(java.io.File(com.mrpowergamerbr.loritta.Loritta.FOLDER + "oswald_regular.ttf")))
					.deriveFont(23F)

			graphics.font = oswaldRegular

			graphics.color = Color(128, 128, 128, 128)
			graphics.drawString(user.name, 82, 22)
			graphics.color = Color(255, 255, 255)
			graphics.drawString(user.name, 82, 22)

			context.sendFile(base.makeRoundedCorners(15), "perfil.png", context.getAsMention(true))
            return
		}

        val profileWrapper = javax.imageio.ImageIO.read(java.io.File(com.mrpowergamerbr.loritta.Loritta.FOLDER + "profile_wrapper.png")); // Wrapper do perfil
        var user = if (context.message.mentionedUsers.size == 1) context.message.mentionedUsers[0] else context.userHandle
        if (user == null) {
            context.sendMessage(context.getAsMention(true) + "Não foi encontrado nenhum usuário com este nome!");
            return;
        }

        if (context.message.mentionedUsers.size == 1) {
            userProfile = com.mrpowergamerbr.loritta.LorittaLauncher.getInstance().getLorittaProfileForUser(context.message.mentionedUsers[0].id)
            userData = context.config.userData.getOrDefault(context.message.mentionedUsers[0].id, LorittaServerUserData());
        }

        var background: BufferedImage?;

        var file = java.io.File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + userProfile.userId + ".png");
        if (file.exists()) {
            background = javax.imageio.ImageIO.read(java.io.File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + userProfile.userId + ".png")); // Background padrão
        } else {
            background = javax.imageio.ImageIO.read(java.io.File(com.mrpowergamerbr.loritta.Loritta.FOLDER + "default_background.png")); // Background padrão
        }

        graphics.drawImage(background, 0, 0, null); // Background fica atrás de tudo

        val imageUrl = java.net.URL(user.effectiveAvatarUrl) // Carregar avatar do usuário
        val connection = imageUrl.openConnection() as java.net.HttpURLConnection
        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0")
        val avatar = javax.imageio.ImageIO.read(connection.inputStream)

        val avatarImg = avatar.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH)

        val bebasNeue = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
                java.io.FileInputStream(java.io.File(com.mrpowergamerbr.loritta.Loritta.FOLDER + "BebasNeue.otf")))

        val bariolRegular = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
                java.io.FileInputStream(java.io.File(com.mrpowergamerbr.loritta.Loritta.FOLDER + "bariol_regular.otf")))

        val mavenProBold = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
                java.io.FileInputStream(java.io.File(com.mrpowergamerbr.loritta.Loritta.FOLDER + "mavenpro-bold.ttf")))

        val guildImages = ArrayList<java.awt.Image>();

        val guilds = LorittaLauncher.getInstance().lorittaShards.getGuilds()
                .filter { guild -> guild.isMember(user) }
				.sortedByDescending { it.members.size }

        var idx = 0;
        for (guild in guilds) {
            if (guild.iconUrl != null) {
                if (idx > 14) {
                    break;
                }
                try {
                    val connection = java.net.URL(guild.iconUrl).openConnection() as java.net.HttpURLConnection
                    connection.setRequestProperty(
                            "User-Agent",
                            "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0")
                    var guild = javax.imageio.ImageIO.read(connection.inputStream)
                    var guildImg = com.mrpowergamerbr.loritta.utils.ImageUtils.toBufferedImage(guild.getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH));
                    guildImg = guildImg.getSubimage(1, 1, guildImg.height - 1, guildImg.width - 1);
                    guildImg = com.mrpowergamerbr.loritta.utils.ImageUtils.makeRoundedCorner(guildImg, 999);
                    guildImages.add(guildImg)
                    idx++;
                } catch (e: Exception) {
                }
            }
        }
        graphics.drawImage(avatarImg, 5, 75, null); // Colar avatar do usuário no profile
        graphics.drawImage(profileWrapper, 0, 0, null); // Colar wrapper (precisa ser o último para ficar certo)
        graphics.setRenderingHint(
                java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        graphics.color = java.awt.Color(211, 211, 211); // Cinza
        graphics.font = bebasNeue.deriveFont(24F);
        graphics.drawString(user.name, 74, 137)
        graphics.color = java.awt.Color(255, 255, 255); // Branca
        graphics.drawString(user.name, 74, 135)

        if (idx > 14) {
            val minecraftia = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
                    java.io.FileInputStream(java.io.File(com.mrpowergamerbr.loritta.Loritta.FOLDER + "minecraftia.ttf")))

            graphics.font = minecraftia.deriveFont(8F);
            graphics.drawString("+" + (guilds.size - 14) + " guilds", 20, 287)
        }

        var guildX = 10;
        var guildY = 151;
        for (guild in guildImages) {
            graphics.drawImage(guild, guildX, guildY, null);
            guildX += 24;

            if (guildX >= 10 + (24 * 3)) {
                guildX = 10;
                guildY += guild.getHeight(null);
            }
        }

        var offset = 16;

        // Barrinha de XP
        graphics.color = java.awt.Color(128, 128, 128)
        graphics.fillRect(87, offset + 143, 148, 19);

        graphics.color = java.awt.Color(0, 0, 0)
        graphics.fillRect(88, offset + 144, 146, 17);

        // Barrinha de XP do servidor
        graphics.color = java.awt.Color(128, 128, 128)
        graphics.fillRect(239, offset + 143, 148, 19);

        graphics.color = java.awt.Color(0, 0, 0)
        graphics.fillRect(240, offset + 144, 146, 17);

        // Calcular quanto a barrinha deveria ficar
        run {
            val xpWrapper = userProfile.getCurrentLevel()
            val nextLevel = userProfile.getExpToAdvanceFrom(userProfile.getCurrentLevel().currentLevel + 1)
            val currentLevel = xpWrapper.expLeft
            val percentage = (currentLevel.toDouble() / nextLevel.toDouble());

            graphics.color = Color(114, 137, 218)
            graphics.fillRect(89, offset +  145, (percentage * 148).toInt(), 15);

            graphics.color = Color(255, 255, 255);

            graphics.font = bariolRegular.deriveFont(10F);
            ImageUtils.drawCenteredStringOutlined(graphics, "$currentLevel/$nextLevel XP", java.awt.Rectangle(89, offset +  145, 148, 15), graphics.font);
        }
        // E agora calcular quanto a barrinha deveria ficar (mas do XP do servidor)
        run {
            val xpWrapper = userData.getCurrentLevel()
            val nextLevel = userData.getExpToAdvanceFrom(userData.getCurrentLevel().currentLevel + 1)
            val currentLevel = xpWrapper.expLeft
            val percentage = (currentLevel.toDouble() / nextLevel.toDouble());

            graphics.color = Color(218, 195, 114)
            graphics.fillRect(241, offset +  145, (percentage * 148).toInt(), 15);

            graphics.color = Color(255, 255, 255);

            graphics.font = bariolRegular.deriveFont(10F);
            ImageUtils.drawCenteredStringOutlined(graphics, "$currentLevel/$nextLevel XP", java.awt.Rectangle(241, offset + 145, 148, 15), graphics.font);
        }

        graphics.font = mavenProBold.deriveFont(20F)
        graphics.color = java.awt.Color(118, 118, 118);
        graphics.drawString("NÍVEL", 92, offset + 180);
        graphics.color = java.awt.Color(90, 90, 90);
        graphics.font = mavenProBold.deriveFont(28F)
        ImageUtils.drawCenteredString(graphics, userProfile.getCurrentLevel().currentLevel.toString(), java.awt.Rectangle(83, offset + 184, 77, 23), graphics.font);
        graphics.color = java.awt.Color(118, 118, 118);
        graphics.font = bariolRegular.deriveFont(12F)
        drawWithShadow("XP Total", 163, offset + 178, 9999, 9999, graphics)
        drawWithShadow("Tempo Online", 163, offset + 193, 9999, 9999, graphics)
        drawWithShadow("Reputação", 163, offset + 208, 9999, 9999, graphics)

        drawWithShadow(userProfile.xp.toString(), 235, offset + 178, 9999, 9999, graphics)

        // Desenhar alguns textos
        graphics.font = bariolRegular.deriveFont(10F)
        drawWithShadow("XP Global", 87, 157, 9999, 9999, graphics)
        drawWithShadow("XP no ${context.guild.name}", 239, 157, 9999, 9999, graphics)

        graphics.font = bariolRegular.deriveFont(12F)
        val hours = userProfile.tempoOnline / 3600;
        val minutes = (userProfile.tempoOnline % 3600) / 60;
        val seconds = userProfile.tempoOnline % 60;

        drawWithShadow("${hours}h${minutes}m${seconds}s", 235, offset + 193, 9999, 9999, graphics)
        drawWithShadow(userProfile.getReputation().toString(), 235, offset + 208, 9999, 9999, graphics)
        graphics.font = bariolRegular.deriveFont(12F)

        var aboutMe = if (Loritta.config.clientId == userProfile.userId) {
            "Olá, eu me chamo Loritta (ou, como meus amigos próximos me chamam, \"Lori\") e sou apenas um simples bot brasileiro para o Discord com várias funções jamais vistas!"
        } else {
            userProfile.aboutMe
        }

        drawWithShadow(aboutMe, 89, 254, 388, 9999, graphics);

        if (!userProfile.games.isEmpty()) {
            graphics.font = bariolRegular.deriveFont(10F)
            val games = ArrayList<GamePlayed>();
            for (entry in userProfile.games.entries) {
                games.add(GamePlayed(entry.key.replace("[---DOT---]", "."), entry.value));
            }

            val sorted = games.sortedWith(compareBy({ it.timeSpent })).reversed();

            drawWithShadow("Jogo mais jogado: " + sorted[0].game, 89, 289, 388, 9999, graphics);
        }

        // Deixar as bordas arredondadas, parece frescura mas com as bordas arrendondadas o perfil parece que é
        // "do Discord" e não simplesmente uma imagem no chat
        base = ImageUtils.makeRoundedCorner(base, 15);

        context.sendFile(base, "profile.png", "📝 **|** " + context.getAsMention(true) + "Perfil"); // E agora envie o arquivo
    }

    fun drawWithShadow(text: String, x: Int, y: Int, maxX: Int, maxY: Int, graphics: Graphics) {
        graphics.color = java.awt.Color(75, 75, 75, 75);
        ImageUtils.drawTextWrapSpaces(text, x, y + 1, maxX, maxY, graphics.fontMetrics, graphics);
        graphics.color = java.awt.Color(118, 118, 118);
        ImageUtils.drawTextWrapSpaces(text, x, y, maxX, maxY, graphics.fontMetrics, graphics);
    }

    data class GamePlayed(val game: String, val timeSpent: Long)
}