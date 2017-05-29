package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.utils.ImageUtils
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.image.BufferedImage

import java.io.File
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO

class BackgroundCommand : com.mrpowergamerbr.loritta.commands.CommandBase() {
    override fun getLabel():String {
        return "background";
    }

    override fun getDescription(): String {
        return "Permite alterar o background do seu perfil!";
    }

    override fun getCategory(): com.mrpowergamerbr.loritta.commands.CommandCategory {
        return com.mrpowergamerbr.loritta.commands.CommandCategory.SOCIAL;
    }

    override fun run(context: com.mrpowergamerbr.loritta.commands.CommandContext) {
        var userProfile = context.lorittaUser.profile

        if (context.args.size == 1) {
            var link = context.args[0];

            try {
                val imageUrl = java.net.URL(link)
                val connection = imageUrl.openConnection() as java.net.HttpURLConnection
                connection.setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0")

                var bufferedImage = javax.imageio.ImageIO.read(connection.inputStream);
                var needsEditing = false;
                if (!(bufferedImage.width == 400 && bufferedImage.height == 300)) {
                    needsEditing = true;
                    if (bufferedImage.width > 400 && bufferedImage.height > 300) {
                        var newWidth = 400.toDouble() / bufferedImage.width.toDouble();
                        var newHeight = 300.toDouble() / bufferedImage.height.toDouble();

                        var use = if (bufferedImage.height > bufferedImage.width) newWidth else newHeight;
                        bufferedImage = com.mrpowergamerbr.loritta.utils.ImageUtils.toBufferedImage(bufferedImage.getScaledInstance((bufferedImage.width * use).toInt(), (bufferedImage.height * use).toInt(), java.awt.image.BufferedImage.SCALE_SMOOTH));
                        bufferedImage = bufferedImage.getSubimage(0, 0, 400, 300);
                    }
                }
                javax.imageio.ImageIO.write(bufferedImage, "png", java.io.File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + userProfile.userId + ".png"));

                context.sendMessage("Background atualizado!" + if (needsEditing) " Como a sua imagem não era 400x300, eu precisei mexer um pouquinho nela!" else "")
                return;
            } catch (e: java.lang.Exception) {
                e.printStackTrace();
                context.sendMessage("Link inválido!");
                return;
            }
        }
        var file = java.io.File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + userProfile.userId + ".png");
        var imageUrl = if (file.exists()) "http://loritta.website/assets/img/backgrounds/" + userProfile.userId + ".png?time=" + System.currentTimeMillis() else "http://loritta.website/assets/img/backgrounds/default_background.png";

        var builder = net.dv8tion.jda.core.EmbedBuilder()
                .setDescription("Altere o seu background colocando o link ao lado do comando!\n\nAtualmente o seu background atual é...")
                .setImage(imageUrl)
                .build();
        context.sendMessage(builder);
    }
}