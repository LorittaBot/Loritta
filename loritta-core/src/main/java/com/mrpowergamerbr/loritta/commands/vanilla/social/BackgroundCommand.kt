package com.mrpowergamerbr.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.awt.Color

class BackgroundCommand : AbstractCommand("background", listOf("papeldeparede"), CommandCategory.SOCIAL) {
	override fun getUsage(): String {
		return "<novo background>"
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["BACKGROUND_DESCRIPTION"]
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_MANAGE, Permission.MESSAGE_ADD_REACTION)
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val link = context.getImageUrlAt(0, 1, 2048)

		if (link != null) {
			setAsBackground(link, context)
			return
		}
		val embed = getFirstPageEmbed(context)
		val message = context.sendMessage(embed)

		message.onReactionAddByAuthor(context) {
			if (it.reactionEmote.isEmote("\uD83D\uDE4B")) { // Caso seja para voltar para a página inicial...
				message.editMessage(getFirstPageEmbed(context)).await()
				message.clearReactions().await()
				message.addReaction("\uD83D\uDDBC").await() // Quadro - Para ver seu background atual
				message.addReaction("\uD83D\uDED2").await() // Carrinho de supermercado - Para procurar novos backgrounds
				return@onReactionAddByAuthor
			}
			if (it.reactionEmote.isEmote("\uD83D\uDDBC")) { // Se é o quadro...
				val file = java.io.File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + context.lorittaUser.profile.userId + ".png")
				val imageUrl = if (file.exists()) "${loritta.instanceConfig.loritta.website.url}assets/img/backgrounds/" + context.lorittaUser.profile.userId + ".png?time=" + System.currentTimeMillis() else "http://loritta.website/assets/img/backgrounds/default_background.png"

				var builder = net.dv8tion.jda.api.EmbedBuilder()
						.setTitle("\uD83D\uDDBC ${context.legacyLocale["BACKGROUND_YOUR_CURRENT_BG"]}")
						.setImage(imageUrl)
						.setColor(Color(0, 223, 142))
				message.editMessage(builder.build()).await()
				message.clearReactions().await()
				message.addReaction("\uD83D\uDE4B").await() // Para voltar para a "página inicial"
				message.addReaction("\uD83D\uDED2").await() // Para ir para os "templates"
				return@onReactionAddByAuthor
			}
			if (it.reactionEmote.isEmote("\uD83D\uDED2") || it.reactionEmote.isEmote("⬅") || it.reactionEmote.isEmote("➡") || it.reactionEmote.isEmote("✅")) { // Se é o carrinho de super mercado...
				val templates = listOf("https://loritta.website/assets/img/templates/dreemurrs.png",
						"https://loritta.website/assets/img/templates/chaves_sexta.png",
						"https://loritta.website/assets/img/templates/rodrigo_noriaki.png",
						"https://loritta.website/assets/img/templates/demencia.png",
						"https://loritta.website/assets/img/templates/nintendo_switch.png",
						"https://loritta.website/assets/img/templates/asriel_alright.png",
						"https://loritta.website/assets/img/templates/parappa_pool.png",
						"https://loritta.website/assets/img/templates/sonic_wisps.png",
						"https://loritta.website/assets/img/templates/gotta_go_fast.png")
				var index = context.metadata.getOrDefault("templateIdx", 0) as Int

				if (it.reactionEmote.isEmote("⬅")) {
					index -= 1
				}
				if (it.reactionEmote.isEmote("➡")) {
					index += 1
				}

				if (index !in 0 until templates.size) {
					index = 0
				}

				var currentUrl = templates[index]

				if (it.reactionEmote.isEmote("✅")) {
					message.delete().await()
					setAsBackground(currentUrl, context)
					return@onReactionAddByAuthor
				}
				context.metadata.put("templateIdx", index)
				var builder = EmbedBuilder()
						.setTitle("\uD83D\uDED2 Templates")
						.setDescription(context.legacyLocale["BACKGROUND_TEMPLATE_INFO"])
						.setImage(currentUrl)
						.setColor(Color(0, 223, 142))

				message.editMessage(builder.build()).await()
				message.clearReactions().await()
				message.addReaction("✅").await()
				message.addReaction("\uD83D\uDE4B").await() // Para voltar para a "página inicial"
				if (index > 0) {
					message.addReaction("⬅").await()
				}
				if (templates.size > index + 1) {
					message.addReaction("➡").await()
				}
			}
		}
		message.addReaction("\uD83D\uDDBC").await() // Quadro - Para ver seu background atual
		message.addReaction("\uD83D\uDED2").await() // Carrinho de supermercado - Para procurar novos backgrounds
	}

	suspend fun setAsBackground(link0: String, context: CommandContext) {
		var link = link0
		var mensagem = context.sendMessage("💭 **|** " + context.getAsMention(true) + "${context.legacyLocale["PROCESSING"]}...")

		val params = getQueryParameters(link)

		if (params.containsKey("imgurl")) {
			link = params["imgurl"]!!
		}

		val status = LorittaUtilsKotlin.getImageStatus(link)

		if (status == NSFWResponse.ERROR) {
			mensagem.editMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.legacyLocale["BACKGROUND_INVALID_IMAGE"]).queue()
			return
		}

		if (status == NSFWResponse.NSFW) {
			mensagem.editMessage("🙅 **|** " + context.getAsMention(true) + context.legacyLocale["NSFW_IMAGE", context.asMention]).queue()
			return
		}

		if (status == NSFWResponse.EXCEPTION) {
			println("* Usuário: ${context.userHandle.name} (${context.userHandle.id})")
		}

		var bufferedImage = LorittaUtils.downloadImage(link) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

		var needsEditing = false
		if (!(bufferedImage.width == 800 && bufferedImage.height == 600)) {
			needsEditing = true
			if (bufferedImage.width > 800 && bufferedImage.height > 600) {
				var newWidth = 800.toDouble() / bufferedImage.width.toDouble()
				var newHeight = 600.toDouble() / bufferedImage.height.toDouble()
				var use = if (bufferedImage.height > bufferedImage.width) newWidth else newHeight
				bufferedImage = com.mrpowergamerbr.loritta.utils.ImageUtils.toBufferedImage(bufferedImage.getScaledInstance((bufferedImage.width * use).toInt(), (bufferedImage.height * use).toInt(), java.awt.image.BufferedImage.SCALE_SMOOTH))
				bufferedImage = bufferedImage.getSubimage(0, 0, Math.min(bufferedImage.width, 800), Math.min(bufferedImage.height, 600))
			}
		}
		javax.imageio.ImageIO.write(bufferedImage, "png", java.io.File(Loritta.FRONTEND, "static/assets/img/backgrounds/" + context.lorittaUser.profile.userId + ".png"))

		context.sendMessage("✨ **|** " + context.getAsMention(true) + context.legacyLocale["BACKGROUND_UPDATED"] + if (needsEditing) " ${context.legacyLocale["BACKGROUND_EDITED"]}!" else "")
		return
	}

	fun getFirstPageEmbed(context: CommandContext): MessageEmbed {
		var builder = net.dv8tion.jda.api.EmbedBuilder()
				.setTitle("\uD83D\uDE4B ${context.legacyLocale["BACKGROUND_CENTRAL"]}")
				.setDescription(context.legacyLocale["BACKGROUND_INFO", context.config.commandPrefix])
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
					queryName = ""
					queryParam = ""
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