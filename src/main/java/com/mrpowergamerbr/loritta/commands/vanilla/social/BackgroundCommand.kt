package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.NSFWResponse
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import org.jsoup.Jsoup
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.StringReader
import java.net.URLDecoder
import java.net.URLEncoder

class BackgroundCommand : com.mrpowergamerbr.loritta.commands.CommandBase() {
	override fun getLabel(): String {
		return "background";
	}

	override fun getUsage(): String {
		return "<novo background>"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.BACKGROUND_DESCRIPTION;
	}

	override fun getCategory(): com.mrpowergamerbr.loritta.commands.CommandCategory {
		return CommandCategory.SOCIAL
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_MANAGE, Permission.MESSAGE_ADD_REACTION)
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
						.setTitle("\uD83D\uDDBC ${context.locale.BACKGROUND_YOUR_CURRENT_BG}")
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
					msg.delete().complete()
					setAsBackground(currentUrl, context)
					return;
				}
				context.metadata.put("templateIdx", index)
				var builder = EmbedBuilder()
						.setTitle("\uD83D\uDED2 Templates")
						.setDescription(context.locale.BACKGROUND_TEMPLATE_INFO)
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

	fun setAsBackground(link0: String, context: CommandContext) {
		var link = link0
		var mensagem = context.sendMessage("💭 **|** " + context.getAsMention(true) + "${context.locale.PROCESSING}...");

		val params = getQueryParameters(link)

		if (params.containsKey("imgurl")) {
			link = params["imgurl"]!!
		}

		val status = LorittaUtilsKotlin.getImageStatus(link)

		if (status == NSFWResponse.ERROR) {
			mensagem.editMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale.BACKGROUND_INVALID_IMAGE).complete()
			return
		}

		if (status == NSFWResponse.NSFW) {
			mensagem.editMessage("🙅 **|** " + context.getAsMention(true) + context.locale.NSFW_IMAGE.msgFormat(context.asMention)).complete()
			return
		}

		if (status == NSFWResponse.EXCEPTION) {
			println("* Usuário: ${context.userHandle.name} (${context.userHandle.id})")
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

		context.sendMessage("✨ **|** " + context.getAsMention(true) + context.locale.BACKGROUND_UPDATED + if (needsEditing) " ${context.locale.BACKGROUND_EDITED}!" else "")
		return;
	}

	fun getFirstPageEmbed(context: CommandContext): MessageEmbed {
		var builder = net.dv8tion.jda.core.EmbedBuilder()
				.setTitle("\uD83D\uDE4B ${context.locale.BACKGROUND_CENTRAL}")
				.setDescription(context.locale.BACKGROUND_INFO.msgFormat(context.config.commandPrefix))
				.setColor(Color(0, 223, 142))
		return builder.build()
	}

	fun getQueryParameters(url: String): Map<String, String> {
		val params = mutableMapOf<String, String>()

		var queryName: String = ""
		var queryParam: String = ""
		var isQueryName = false
		var isQueryParam = false
		for (char in url) {
			if (char == '=') {
				isQueryName = false
				isQueryParam = true
				continue
			}
			if (char == '&' || char == '?') {
				isQueryName = true
				if (isQueryParam) {
					params.put(queryName, queryParam)
					queryName = "";
					queryParam = "";
					isQueryParam = false
				}
				continue
			}
			if (isQueryName) {
				queryName += char
			}
			if (isQueryParam) {
				queryParam += char
			}
		}
		return params
	}
}