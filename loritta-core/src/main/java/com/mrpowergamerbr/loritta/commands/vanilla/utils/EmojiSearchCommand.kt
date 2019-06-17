package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.vanilla.discord.EmojiInfoCommand
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Emote
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream

class EmojiSearchCommand : AbstractCommand("emojisearch", listOf("procuraremoji", "buscaremoji", "findemoji", "emojifinder", "searchemoji"), CommandCategory.UTILS) {
	companion object {
	    const val EMOTES_PER_PAGE = 10
	}

	override fun getUsage(): String {
		return "query [animated]"
	}

	override fun getExamples(): List<String> {
		return listOf("think")
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["EMOJISEARCH_Description"]
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val query = context.rawArgs[0]

			if (3 > query.length) {
				context.reply(
						LoriReply(
								locale["EMOJISEARCH_SmallQuery"],
								Constants.ERROR
						)
				)
				return
			}

			// verifica se o ultimo argumento é animated, caso verdadeito só retorna emojos animados
			val onlyAnimated = context.args[context.args.size - 1] == "animated"

			val queriedEmotes = lorittaShards.getGuilds()
					.flatMap { it ->
						it.emoteCache.filter {
							it.name.contains(query, true) && ((onlyAnimated && it.isAnimated) || !onlyAnimated) && it.canInteract(it.guild?.selfMember) // Se canInteract for false, então a Lori não irá conseguir adicionar ela como reação
						}
					}.sortedByDescending { it.guild?.memberCache?.size() ?: 0 }

			sendQueriedEmbed(context, queriedEmotes, query, 0)
		} else {
			context.explain()
		}
	}

	suspend fun sendQueriedEmbed(context: CommandContext, _queriedEmotes: List<Emote>, query: String, page: Int): Message {
		val emotesPreview = BufferedImage(333, 128, BufferedImage.TYPE_INT_ARGB)
		val graphics = emotesPreview.graphics

		val totalPages = (_queriedEmotes.size / EMOTES_PER_PAGE)
		val queriedEmotes = _queriedEmotes.subList(page * EMOTES_PER_PAGE, Math.min(_queriedEmotes.size, (page + 1) * EMOTES_PER_PAGE))
		var x = 0
		var y = 0

		for (emote in queriedEmotes) {
			val url = emote.imageUrl
			val emoteImage = LorittaUtils.downloadImage(url)

			if (x + 64 > 333) {
				x = 0
				y += 64
			}

			if (emoteImage != null) {
				graphics.drawImage(emoteImage.getScaledInstance(64, 64, BufferedImage.SCALE_SMOOTH), x, y, null)
			}
			x += 64
		}

		val embed = EmbedBuilder().apply {
			setTitle("<:lori_pac:503600573741006863> ${context.legacyLocale["EMOJISEARCH_Title"]}")
			setDescription(context.legacyLocale["EMOJISEARCH_Results", _queriedEmotes.size, query])
			setColor(Constants.DISCORD_BLURPLE)
			setImage("attachment://emotes.png")
			setFooter("${context.legacyLocale["LORITTA_PageOf", page + 1, totalPages + 1]} | ${queriedEmotes.size} emojis", null)
		}

		val message = context.sendFile(emotesPreview, "emotes.png", embed.build())

		message.onReactionAddByAuthor(context) {
			message.delete().queue()

			if (it.reactionEmote.isEmote) {
				val emote = queriedEmotes.firstOrNull { queriedEmote -> queriedEmote.idLong == it.reactionEmote?.emote?.idLong }

				if (emote == null) {
					// TODO: Adicionar mensagem avisando que o emote foi deletado
					return@onReactionAddByAuthor
				}

				val emojiInfoEmbed = EmojiInfoCommand.getDiscordEmoteInfoEmbed(context, emote)

				val emoteInfo = context.sendMessage(emojiInfoEmbed)

				emoteInfo.onReactionAddByAuthor(context) {
					if (it.reactionEmote.isEmote("⏪")) {
						emoteInfo.delete().queue()
						sendQueriedEmbed(context, _queriedEmotes, query, page)
					}
					if (context.guild.selfMember.hasPermission(Permission.MANAGE_EMOTES) && context.handle.hasPermission(Permission.MANAGE_EMOTES)) {
						if (it.reactionEmote.isEmote("wumplus")) {
							emoteInfo.delete().queue()
							try {
								ByteArrayOutputStream().use { os ->
									val os = LorittaUtils.downloadFile(emote.imageUrl, 5000) ?: throw RuntimeException("Couldn't download image!")

									os.use { inputStream ->
										val sentEmote = context.guild.createEmote(emote.name, Icon.from(inputStream)).await()
										context.reply(
												LoriReply(
														context.legacyLocale["EMOJISEARCH_AddSuccess"],
														sentEmote.asMention
												)
										)
									}
								}
							} catch (e: Exception) {
								context.reply(
										LoriReply(
												context.legacyLocale["EMOJISEARCH_AddError"],
												Constants.ERROR
										)
								)
							}
						}
					}
				}

				emoteInfo.addReaction("⏪").queue()

				if (context.guild.selfMember.hasPermission(Permission.MANAGE_EMOTES) && context.handle.hasPermission(Permission.MANAGE_EMOTES)) {
					emoteInfo.addReaction("wumplus:388417805126467594").queue()
				}
			} else {
				if (it.reactionEmote.isEmote("⏩")) {
					sendQueriedEmbed(context, _queriedEmotes, query, 1 + page)
				} else if (it.reactionEmote.isEmote("⏪")) {
					sendQueriedEmbed(context, _queriedEmotes, query, page - 1)
				}
			}
		}

		for (emote in queriedEmotes) { // Adicionar reações da pesquisa
			message.addReaction(emote).queue()
		}

		if (page > 0) {
			message.addReaction("⏪").queue()
		}
		if (queriedEmotes.size >= 9) {
			message.addReaction("⏩").queue()
		}

		return message
	}
}
