package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import java.awt.image.BufferedImage
import java.io.StringReader
import java.net.URLEncoder

class BackgroundCommand : com.mrpowergamerbr.loritta.commands.CommandBase() {
    override fun getLabel(): String {
        return "background";
    }

    override fun getUsage(): String {
        return "<novo background>"
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
                var mensagem = context.sendMessage("💭 | " + context.getAsMention(true) + "Processando...");

                var response = HttpRequest.get("https://mdr8.p.mashape.com/api/?url=" + URLEncoder.encode(link, "UTF-8"))
                        .header("X-Mashape-Key", Loritta.config.mashapeKey)
                        .header("Accept", "application/json")
                        .acceptJson()
                        .body()

				val reader = StringReader(response)
				val jsonReader = JsonReader(reader)
				val apiResponse = JsonParser().parse(jsonReader).asJsonObject // Base

				if (apiResponse.get("rating_label").asString == "adult") {
					mensagem.editMessage("🙅 | " + context.getAsMention(true) + "**Imagem pornográfica (NSFW) detectada!**\n\nQue feio... Sério mesmo que você queria usar *isto* como seu background? Você acha mesmo que alguém vai ver seu background e vai falar \"nossa, o " + context.getAsMention(false) + " é maravilhoso porque ele gasta o tempo dele vendo pessoas se pegando porque ele não consegue pegar ninguém!\"?\n\nNão, ninguém irá falar isto, mude sua vida, pare de fazer isto.\n\n(Se isto foi um falso positivo então... sei lá, me ignore 😞)").complete()
					return;
				}

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
                        bufferedImage = bufferedImage.getSubimage(0, 0, Math.min(bufferedImage.width, 400), Math.min(bufferedImage.height, 300));
                    }
                }
                javax.imageio.ImageIO.write(bufferedImage, "png", java.io.File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + userProfile.userId + ".png"));

                context.sendMessage("✨ | " + context.getAsMention(true) + "Background atualizado! (${apiResponse.get("rating_label").asString})" + if (needsEditing) " Como a sua imagem não era 400x300, eu precisei mexer um pouquinho nela!" else "")
                return;
            } catch (e: java.lang.Exception) {
                e.printStackTrace();
                context.sendMessage(LorittaUtils.ERROR + " | " +context.getAsMention(true) + "Link inválido! (Não se esqueça, você precisa enviar o link direto para a imagem!)");
                return;
            }
        }
        var file = java.io.File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + userProfile.userId + ".png");
        var imageUrl = if (file.exists()) "http://loritta.website/assets/img/backgrounds/" + userProfile.userId + ".png?time=" + System.currentTimeMillis() else "http://loritta.website/assets/img/backgrounds/default_background.png";

        var builder = net.dv8tion.jda.core.EmbedBuilder()
                .setDescription("Altere o seu background colocando o link ao lado do comando!\n\nAtualmente o seu background é...\n\n(Backgrounds NSFW terão a sua conta banida!)")
                .setImage(imageUrl)
                .build();
        context.sendMessage(builder);
    }
}