package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import org.apache.commons.io.FileUtils
import com.mrpowergamerbr.loritta.Loritta
import org.apache.commons.lang3.StringEscapeUtils
import java.awt.*
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Paths

import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import javax.imageio.ImageIO


class PerfilCommand : CommandBase() {
    override fun getLabel():String {
        return "perfil";
    }

    override fun run(context: CommandContext) {
        val base = BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB); // Base
        val graphics = base.graphics as Graphics2D;
        val profileWrapper = ImageIO.read(File(Loritta.FOLDER + "profile_wrapper.png")); // Wrapper do perfil
        val defaultBackground = ImageIO.read(File(Loritta.FOLDER + "default_background.png")); // Background padrão

        graphics.drawImage(defaultBackground, 0, 0, null); // Background fica atrás de tudo

        val imageUrl = URL(context.userHandle.effectiveAvatarUrl) // Carregar avatar do usuário
        val connection = imageUrl.openConnection() as HttpURLConnection
        connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0")
        val avatar = ImageIO.read(connection.inputStream)

        val avatarImg = avatar.getScaledInstance(64, 64, Image.SCALE_SMOOTH)

        val bebasNeue = Font.createFont(Font.TRUETYPE_FONT,
                FileInputStream(File(Loritta.FOLDER + "BebasNeue.otf")))

        graphics.drawImage(avatarImg, 5, 65, null); // Colar avatar do usuário no profile
        graphics.drawImage(profileWrapper, 0, 0, null); // Colar wrapper (precisa ser o último para ficar certo)
        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        graphics.color = Color(60, 60, 60); // Preto
        graphics.font = bebasNeue.deriveFont(24F);
        graphics.drawString(context.userHandle.name, 78, 122)
        graphics.color = Color(100, 100, 100); // Cinza
        graphics.font = bebasNeue.deriveFont(24F);
        graphics.drawString(context.userHandle.name, 78, 120)

        val os = ByteArrayOutputStream()
        ImageIO.write(base, "png", os)
        val inputStream = ByteArrayInputStream(os.toByteArray())

        context.sendFile(inputStream, "profile.png", " "); // E agora envie o arquivo
    }
}