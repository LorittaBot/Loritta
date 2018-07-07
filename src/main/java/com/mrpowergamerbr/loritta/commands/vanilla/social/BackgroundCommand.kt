package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.MessageEmbed
import java.awt.Color

class BackgroundCommand : AbstractCommand("background", listOf("papeldeparede"), CommandCategory.SOCIAL) {
	override fun getUsage(): String {
		return "<novo background>"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["BACKGROUND_DESCRIPTION"]
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_MANAGE, Permission.MESSAGE_ADD_REACTION)
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val link = context.getImageUrlAt(0, 1, 2048);

		if (link != null) {
			setAsBackground(link, context);
			return;
		}
		val embed = getFirstPageEmbed(context)
		val message = context.sendMessage(embed);

		message.onReactionAddByAuthor(context) {
			if (it.reactionEmote.name == "\uD83D\uDE4B") { // Caso seja para voltar para a página inicial...
				message.editMessage(getFirstPageEmbed(context)).complete();
				message.clearReactions().complete()
				message.addReaction("\uD83D\uDDBC").complete() // Quadro - Para ver seu background atual
				message.addReaction("\uD83D\uDED2").complete() // Carrinho de supermercado - Para procurar novos backgrounds
				return@onReactionAddByAuthor
			}
			if (it.reactionEmote.name == "\uD83D\uDDBC") { // Se é o quadro...
				val file = java.io.File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + context.lorittaUser.profile.userId + ".png");
				val imageUrl = if (file.exists()) "${Loritta.config.websiteUrl}assets/img/backgrounds/" + context.lorittaUser.profile.userId + ".png?time=" + System.currentTimeMillis() else "http://loritta.website/assets/img/backgrounds/default_background.png";

				var builder = net.dv8tion.jda.core.EmbedBuilder()
						.setTitle("\uD83D\uDDBC ${context.locale["BACKGROUND_YOUR_CURRENT_BG"]}")
						.setImage(imageUrl)
						.setColor(Color(0, 223, 142))
				message.editMessage(builder.build()).complete()
				message.clearReactions().complete()
				message.addReaction("\uD83D\uDE4B").complete() // Para voltar para a "página inicial"
				message.addReaction("\uD83D\uDED2").complete() // Para ir para os "templates"
				return@onReactionAddByAuthor
			}
			if (it.reactionEmote.name == "\uD83D\uDED2" || it.reactionEmote.name == "⬅" || it.reactionEmote.name == "➡" || it.reactionEmote.name == "✅") { // Se é o carrinho de super mercado...
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

				if (it.reactionEmote.name == "⬅") {
					index -= 1;
				}
				if (it.reactionEmote.name == "➡") {
					index += 1;
				}

				if (index !in 0 until templates.size) {
					index = 0
				}

				var currentUrl = templates[index];

				if (it.reactionEmote.name == "✅") {
					message.delete().complete()
					setAsBackground(currentUrl, context)
					return@onReactionAddByAuthor
				}
				context.metadata.put("templateIdx", index)
				var builder = EmbedBuilder()
						.setTitle("\uD83D\uDED2 Templates")
						.setDescription(context.locale["BACKGROUND_TEMPLATE_INFO"])
						.setImage(currentUrl)
						.setColor(Color(0, 223, 142))

				message.editMessage(builder.build()).complete();
				message.clearReactions().complete()
				message.addReaction("✅").complete();
				message.addReaction("\uD83D\uDE4B").complete(); // Para voltar para a "página inicial"
				if (index > 0) {
					message.addReaction("⬅").complete();
				}
				if (templates.size > index + 1) {
					message.addReaction("➡").complete();
				}
			}
		}
		message.addReaction("\uD83D\uDDBC").complete() // Quadro - Para ver seu background atual
		message.addReaction("\uD83D\uDED2").complete() // Carrinho de supermercado - Para procurar novos backgrounds
	}

	fun setAsBackground(link0: String, context: CommandContext) {
		var link = link0
		var mensagem = context.sendMessage("💭 **|** " + context.getAsMention(true) + "${context.locale["PROCESSING"]}...");

		val params = getQueryParameters(link)

		if (params.containsKey("imgurl")) {
			link = params["imgurl"]!!
		}

		val status = LorittaUtilsKotlin.getImageStatus(link)

		if (status == NSFWResponse.ERROR) {
			mensagem.editMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["BACKGROUND_INVALID_IMAGE"]).complete()
			return
		}

		if (status == NSFWResponse.NSFW) {
			mensagem.editMessage("🙅 **|** " + context.getAsMention(true) + context.locale["NSFW_IMAGE", context.asMention]).complete()
			return
		}

		if (status == NSFWResponse.EXCEPTION) {
			println("* Usuário: ${context.userHandle.name} (${context.userHandle.id})")
		}

		var bufferedImage = LorittaUtils.downloadImage(link) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		var needsEditing = false;
		if (!(bufferedImage.width == 800 && bufferedImage.height == 600)) {
			needsEditing = true;
			if (bufferedImage.width > 800 && bufferedImage.height > 600) {
				var newWidth = 800.toDouble() / bufferedImage.width.toDouble();
				var newHeight = 600.toDouble() / bufferedImage.height.toDouble();
				var use = if (bufferedImage.height > bufferedImage.width) newWidth else newHeight;
				bufferedImage = com.mrpowergamerbr.loritta.utils.ImageUtils.toBufferedImage(bufferedImage.getScaledInstance((bufferedImage.width * use).toInt(), (bufferedImage.height * use).toInt(), java.awt.image.BufferedImage.SCALE_SMOOTH));
				bufferedImage = bufferedImage.getSubimage(0, 0, Math.min(bufferedImage.width, 800), Math.min(bufferedImage.height, 600))
			}
		}
		javax.imageio.ImageIO.write(bufferedImage, "png", java.io.File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + context.lorittaUser.profile.userId + ".png"));

		context.sendMessage("✨ **|** " + context.getAsMention(true) + context.locale["BACKGROUND_UPDATED"] + if (needsEditing) " ${context.locale["BACKGROUND_EDITED"]}!" else "")
		return;
	}

	fun getFirstPageEmbed(context: CommandContext): MessageEmbed {
		var builder = net.dv8tion.jda.core.EmbedBuilder()
				.setTitle("\uD83D\uDE4B ${context.locale["BACKGROUND_CENTRAL"]}")
				.setDescription(context.locale["BACKGROUND_INFO", context.config.commandPrefix])
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