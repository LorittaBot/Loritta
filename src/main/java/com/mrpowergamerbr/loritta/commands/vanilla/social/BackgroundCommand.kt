package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import java.awt.Color
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

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

    override fun run(context: com.mrpowergamerbr.loritta.commands.CommandContext) {
        var userProfile = context.lorittaUser.profile

        val link = LorittaUtils.getURLFromContext(context, 0, 1, 2048);

        if (link != null) {
			setAsBackground(link, context);
            return;
        }
        var embed = getFirstPageEmbed(context)
        val message = context.sendMessage(embed);

		message.addReaction("\uD83D\uDDBC").complete() // Quadro - Para ver seu background atual
		message.addReaction("\uD83D\uDED2").complete() // Carrinho de supermercado - Para procurar novos backgrounds
    }

	override fun onCommandReactionFeedback(context: CommandContext, e: GenericMessageReactionEvent, msg: Message) {
		if (e.user == context.userHandle) { // Somente o usuário que executou o comando pode interagir com o comando!
			if (e.reactionEmote.name == "\uD83D\uDE4B") { // Caso seja para voltar para a página inicial...
				msg.editMessage(getFirstPageEmbed(context)).complete();
				msg.clearReactions().complete()
				msg.addReaction("\uD83D\uDDBC").complete() // Quadro - Para ver seu background atual
				msg.addReaction("\uD83D\uDED2").complete() // Carrinho de supermercado - Para procurar novos backgrounds
				return;
			}
			if (e.reactionEmote.name == "\uD83D\uDDBC") { // Se é o quadro...
				val file = java.io.File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + context.lorittaUser.profile.userId + ".png");
				val imageUrl = if (file.exists()) "http://loritta.website/assets/img/backgrounds/" + context.lorittaUser.profile.userId + ".png?time=" + System.currentTimeMillis() else "http://loritta.website/assets/img/backgrounds/default_background.png";

				var builder = net.dv8tion.jda.core.EmbedBuilder()
						.setTitle("\uD83D\uDDBC Seu background atual")
						.setImage(imageUrl)
						.setColor(Color(0, 223, 142))
				msg.editMessage(builder.build()).complete();
				msg.clearReactions().complete()
				msg.addReaction("\uD83D\uDE4B").complete(); // Para voltar para a "página inicial"
				msg.addReaction("\uD83D\uDED2").complete(); // Para ir para os "templates"
				return;
			}
			if (e.reactionEmote.name == "\uD83D\uDED2" || e.reactionEmote.name == "⬅" || e.reactionEmote.name == "➡" || e.reactionEmote.name == "✅") { // Se é o carrinho de super mercado...
				val templates = listOf("https://loritta.website/assets/img/templates/dreemurrs.png",
						"https://loritta.website/assets/img/templates/chaves_sexta.png",
						"https://loritta.website/assets/img/templates/rodrigo_noriaki.png",
						"https://loritta.website/assets/img/templates/demencia.png",
						"https://loritta.website/assets/img/templates/nintendo_switch.png",
						"https://loritta.website/assets/img/templates/asriel_alright.png",
						"https://loritta.website/assets/img/templates/parappa_pool.png",
						"https://loritta.website/assets/img/templates/sonic_wisps.png",
						"https://loritta.website/assets/img/templates/gotta_go_fast.png")
				var index = context.metadata.getOrDefault("templateIdx", 0) as Int;

				if (e.reactionEmote.name == "⬅") {
					index -= 1;
				}
				if (e.reactionEmote.name == "➡") {
					index += 1;
				}

				var currentUrl = templates[index];

				if (e.reactionEmote.name == "✅") {
					setAsBackground(currentUrl, context)
					return;
				}
				context.metadata.put("templateIdx", index)
				var builder = EmbedBuilder()
						.setTitle("\uD83D\uDED2 Templates")
						.setDescription("Clique em ⬅ para voltar um template\n" +
								"Clique em ➡ para avançar um template\n" +
								"Clique em ✅ para usar este template como seu background")
						.setImage(currentUrl)
						.setColor(Color(0, 223, 142))

				msg.editMessage(builder.build()).complete();
				msg.clearReactions().complete()
				msg.addReaction("✅").complete();
				msg.addReaction("\uD83D\uDE4B").complete(); // Para voltar para a "página inicial"
				if (index > 0) {
					msg.addReaction("⬅").complete();
				}
				if (templates.size > index + 1) {
					msg.addReaction("➡").complete();
				}
			}
		}
	}

	fun setAsBackground(link: String, context: CommandContext) {
		var mensagem = context.sendMessage("💭 **|** " + context.getAsMention(true) + "Processando...");

		var response = HttpRequest.get("https://mdr8.p.mashape.com/api/?url=" + URLEncoder.encode(link, "UTF-8"))
				.header("X-Mashape-Key", Loritta.config.mashapeKey)
				.header("Accept", "application/json")
				.acceptJson()
				.body()

		val reader = StringReader(response)
		val jsonReader = JsonReader(reader)
		val apiResponse = JsonParser().parse(jsonReader).asJsonObject // Base

		if (apiResponse.has("error")) {
			mensagem.editMessage(LorittaUtils.ERROR + " **|** " + context.getAsMention(true) + "Imagem inválida! Tem certeza que isto é um link válido? Se puder, baixe a imagem e faça upload diretamente no Discord!").complete()
			return;
		}

		if (apiResponse.get("rating_label").asString == "adult") {
			mensagem.editMessage("🙅 **|** " + context.getAsMention(true) + "**Imagem pornográfica (NSFW) detectada!**\n\nQue feio... Sério mesmo que você queria usar *isto* como seu background? Você acha mesmo que alguém vai ver seu background e vai falar \"nossa, o " + context.getAsMention(false) + " é maravilhoso porque ele gasta o tempo dele vendo pessoas se pegando porque ele não consegue pegar ninguém!\"?\n\nNão, ninguém irá falar isto, mude sua vida, pare de fazer isto.\n\n(Se isto foi um falso positivo então... sei lá, me ignore 😞)").complete()
			return;
		}

		var bufferedImage = LorittaUtils.downloadImage(link)
		if (!LorittaUtils.isValidImage(context, bufferedImage)) {
			return;
		}
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
		javax.imageio.ImageIO.write(bufferedImage, "png", java.io.File("/home/servers/loritta/frontend/static/assets/img/backgrounds/" + context.lorittaUser.profile.userId + ".png"));

		context.sendMessage("✨ **|** " + context.getAsMention(true) + "Background atualizado! (${apiResponse.get("rating_label").asString})" + if (needsEditing) " Como a sua imagem não era 400x300, eu precisei mexer um pouquinho nela!" else "")
		return;
	}

	fun getFirstPageEmbed(context: CommandContext): MessageEmbed {
		var builder = net.dv8tion.jda.core.EmbedBuilder()
				.setTitle("\uD83D\uDE4B Central de Papéis de Parede")
				.setDescription("**Querendo alterar o seu background do seu perfil? Então você veio ao lugar certo!**\n" +
						"\n" +
						"Clique em \uD83D\uDDBC para ver seu background atual\n" +
						"Clique em \uD83D\uDED2 para ver os templates padrões" +
						"\n" +
						"\n" +
						"Querendo enviar seu próprio background? Sem problemas! Envie uma imagem 400x300 no chat e, junto com a imagem, escreva `" + context.config.commandPrefix + "background`! (Você também pode enviar o link da imagem junto com o comando que eu também irei aceitar!)\n\n(Não envie backgrounds com coisas NSFW! Se você enviar, sua conta será banida de usar qualquer funcionalidade minha!)")
				.setColor(Color(0, 223, 142))
		return builder.build()
	}
}