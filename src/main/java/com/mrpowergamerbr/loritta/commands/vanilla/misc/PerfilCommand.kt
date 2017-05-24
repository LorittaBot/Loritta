package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.utils.ImageUtils
import org.apache.commons.codec.binary.Base64
import java.awt.*
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO

class PerfilCommand : CommandBase() {
    override fun getLabel():String {
        return "perfil";
    }

    override fun run(context: CommandContext) {
        val base = BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB); // Base
        val graphics = base.graphics as Graphics2D;
        val profileWrapper = ImageIO.read(File(Loritta.FOLDER + "profile_wrapper.png")); // Wrapper do perfil
        var userProfile = context.lorittaUser.profile
        var user = if (context.message.mentionedUsers.size == 1) context.message.mentionedUsers[0] else context.userHandle
        if (user == null) {
            context.sendMessage(context.getAsMention(true) + "Não foi encontrado nenhum usuário com este nome!");
            return;
        }

        if (context.message.mentionedUsers.size == 1) {
            userProfile = LorittaProfile(context.message.mentionedUsers[0].id)
        }

        var background: BufferedImage?;

        if (userProfile.userId == Loritta.config.ownerId) {
           background = ImageIO.read(File(Loritta.FOLDER + "shantae_bg.png")); // Background padrão
        } else {
           background = ImageIO.read(File(Loritta.FOLDER + "default_background.png")); // Background padrão
        }

        graphics.drawImage(background, 0, 0, null); // Background fica atrás de tudo

        val imageUrl = URL(user.effectiveAvatarUrl) // Carregar avatar do usuário
        val connection = imageUrl.openConnection() as HttpURLConnection
        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0")
        val avatar = ImageIO.read(connection.inputStream)

        val avatarImg = avatar.getScaledInstance(64, 64, Image.SCALE_SMOOTH)

        val bebasNeue = Font.createFont(Font.TRUETYPE_FONT,
                FileInputStream(File(Loritta.FOLDER + "BebasNeue.otf")))

        val guildImages = ArrayList<Image>();

        val guilds = LorittaLauncher.getInstance().jda.guilds.filter { guild -> guild.isMember(user) };

        var idx = 0;
        for (guild in guilds) {
            if (guild.iconUrl != null) {
                if (idx > 14) {
                    break;
                }
                val connection = URL(guild.iconUrl).openConnection() as HttpURLConnection
                connection.setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0")
                var guild = ImageIO.read(connection.inputStream)
                var guildImg = ImageUtils.toBufferedImage(guild.getScaledInstance(24, 24, Image.SCALE_SMOOTH));
                guildImg = guildImg.getSubimage(1, 1, guildImg.height - 1, guildImg.width - 1);
                guildImg = ImageUtils.makeRoundedCorner(guildImg, 999);
                guildImages.add(guildImg)
                idx++;
            }
        }
        graphics.drawImage(avatarImg, 5, 65, null); // Colar avatar do usuário no profile
        graphics.drawImage(profileWrapper, 0, 0, null); // Colar wrapper (precisa ser o último para ficar certo)
        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        graphics.color = Color(211, 211, 211); // Cinza
        graphics.font = bebasNeue.deriveFont(24F);
        graphics.drawString(user.name, 78, 122)
        graphics.color = Color(255, 255, 255); // Branca
        graphics.font = bebasNeue.deriveFont(24F);
        graphics.drawString(user.name, 78, 120)
        graphics.font = bebasNeue.deriveFont(24F);

        if (idx > 14) {
            val minecraftia = Font.createFont(Font.TRUETYPE_FONT,
                    FileInputStream(File(Loritta.FOLDER + "minecraftia.ttf")))

            graphics.font = minecraftia.deriveFont(8F);
            graphics.drawString("+" + (guilds.size - 14) + " guilds", 20, 277)
        }

        var guildX = 10;
        var guildY = 141;
        for (guild in guildImages) {
            graphics.drawImage(guild, guildX, guildY, null);
            guildX += 24;

            if (guildX >= 10 + (24 * 3)) {
                guildX = 10;
                guildY += guild.getHeight(null);
            }
        }

        val os = ByteArrayOutputStream()
        ImageIO.write(base, "png", os)
        val inputStream = ByteArrayInputStream(os.toByteArray())

        context.sendFile(inputStream, "profile.png", "📝 | Perfil"); // E agora envie o arquivo
    }
}