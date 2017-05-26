package com.mrpowergamerbr.loritta.commands.vanilla.misc

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

class BackgroundCommand : CommandBase() {
    override fun getLabel():String {
        return "background";
    }

    override fun getDescription(): String {
        return "Permite alterar o background do seu perfil!";
    }

    override fun getCategory(): CommandCategory {
        return CommandCategory.SOCIAL;
    }

    override fun run(context: CommandContext) {
        var userProfile = context.lorittaUser.profile

        if (context.args.size == 1) {
            var link = context.args[0];

            try {
                val imageUrl = URL(link)
                val connection = imageUrl.openConnection() as HttpURLConnection
                connection.setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:53.0) Gecko/20100101 Firefox/53.0")

                var bufferedImage = ImageIO.read(connection.inputStream);
                var needsEditing = false;
                if (!(bufferedImage.width == 300 && bufferedImage.height == 300)) {
                    needsEditing = true;
                    if (bufferedImage.width > 300 && bufferedImage.height > 300) {
                        var newWidth = 300.toDouble() / bufferedImage.width.toDouble();
                        var newHeight = 300.toDouble() / bufferedImage.height.toDouble();

                        var use = if (bufferedImage.height > bufferedImage.width) newWidth else newHeight;
                        println(newWidth.toString() + ", " + newHeight.toString());
                        bufferedImage = ImageUtils.toBufferedImage(bufferedImage.getScaledInstance((bufferedImage.width * use).toInt(), (bufferedImage.height * use).toInt(), BufferedImage.SCALE_SMOOTH));
                        bufferedImage = bufferedImage.getSubimage(0, 0, 300, 300);
                    }
                }
                ImageIO.write(bufferedImage, "png", File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + userProfile.userId + ".png"));

                context.sendMessage("Background atualizado!" + if (needsEditing) " Como a sua imagem não era 300x300, eu precisei mexer um pouquinho nela!" else "")
                return;
            } catch (e: Exception) {
                e.printStackTrace();
                context.sendMessage("Link inválido!");
                return;
            }
        }
        var file = File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + userProfile.userId + ".png");
        var imageUrl = if (file.exists()) "http://loritta.website/assets/img/backgrounds/" + userProfile.userId + ".png?time=" + System.currentTimeMillis() else "http://loritta.website/assets/img/backgrounds/default_background.png";

        var builder = EmbedBuilder()
                .setDescription("Altere o seu background colocando o link ao lado do comando!\n\nAtualmente o seu background atual é...")
                .setImage(imageUrl)
                .build();
        context.sendMessage(builder);
    }
}