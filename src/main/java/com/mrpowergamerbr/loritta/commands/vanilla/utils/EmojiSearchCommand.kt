package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.vanilla.discord.EmojiInfoCommand
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Emote
import net.dv8tion.jda.core.entities.Icon
import net.dv8tion.jda.core.entities.Message
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream

class EmojiSearchCommand : AbstractCommand("emojisearch", listOf("procuraremoji", "buscaremoji", "findemoji", "emojifinder"), CommandCategory.UTILS) {
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
						it.emotes.filter {
							it.name.contains(query, true)  && ((onlyAnimated && it.isAnimated) || !onlyAnimated)
						}
					}.sortedByDescending { it.guild.members.size }

			sendQueriedEmbed(context, queriedEmotes, query, 0)
		} else {
			context.explain()
		}
	}

	suspend fun sendQueriedEmbed(context: CommandContext, _queriedEmotes: List<Emote>, query: String, page: Int): Message {
		val emotesPreview = BufferedImage(333, 128, BufferedImage.TYPE_INT_ARGB)
		val graphics = emotesPreview.graphics

		val totalPages = (_queriedEmotes.size / 9)
		val queriedEmotes = _queriedEmotes.subList(page * 9, Math.min(_queriedEmotes.size, (page + 1) * 9))
		var x = 0
		var y = 0
		for ((index, emote) in queriedEmotes.withIndex()) {
			val url = emote.imageUrl
			val emoteImage = LorittaUtils.downloadImage(url)

			if (x + 64 > 333) {
				x = 32
				y += 64
			}

			if (emoteImage != null) {
				graphics.drawImage(emoteImage.getScaledInstance(64, 64, BufferedImage.SCALE_SMOOTH), x, y, null)
			}
			x += 64
		}

		val embed = EmbedBuilder().apply {
			setTitle("<:lori_pac:503600573741006863> ${context.locale["EMOJISEARCH_Title"]}")
			setDescription(context.locale["EMOJISEARCH_Results", _queriedEmotes.size, query])
			setColor(Constants.DISCORD_BLURPLE)
			setImage("attachment://emotes.png")
			setFooter("${context.locale["LORITTA_PageOf", page + 1, totalPages + 1]} | ${queriedEmotes.size} emojis", null)
		}

		val message = context.sendFile(emotesPreview, "emotes.png", embed.build())

		message.onReactionAddByAuthor(context) {
			var index = -1

			for ((i, emote) in Constants.INDEXES.withIndex()) {
				if (emote == it.reactionEmote.name) {
					index = i
					break
				}
			}

			message.delete().queue()

			if (index != -1) {
				val emote = queriedEmotes[index]

				val emojiInfoEmbed = EmojiInfoCommand.getDiscordEmoteInfoEmbed(context, emote)

				val emoteInfo = context.sendMessage(emojiInfoEmbed)

				emoteInfo.onReactionAddByAuthor(context) {
					if (it.reactionEmote.name == "⏪") {
						emoteInfo.delete().queue()
						sendQueriedEmbed(context, _queriedEmotes, query, page)
					}
					if (context.guild.selfMember.hasPermission(Permission.MANAGE_EMOTES) && context.handle.hasPermission(Permission.MANAGE_EMOTES)) {
						if (it.reactionEmote.name == "wumplus") {
							emoteInfo.delete().queue()
							try {
								ByteArrayOutputStream().use { os ->
									val os = LorittaUtils.downloadFile(emote.imageUrl, 5000)

									os.use { inputStream ->
										val sentEmote = context.guild.controller.createEmote(emote.name, Icon.from(inputStream)).await()
										context.reply(
												LoriReply(
														context.locale["EMOJISEARCH_AddSuccess"],
														sentEmote.asMention
												)
										)
									}
								}
							} catch (e: Exception) {
								context.reply(
										LoriReply(
												context.locale["EMOJISEARCH_AddError"],
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
				if (it.reactionEmote.name == "⏩") {
					sendQueriedEmbed(context, _queriedEmotes, query, 1 + page)
				} else if (it.reactionEmote.name == "⏪") {
					sendQueriedEmbed(context, _queriedEmotes, query, page - 1)
				}
			}
		}

		for ((index, emote) in Constants.INDEXES.withIndex()) {
			if (queriedEmotes.size > index) {
				message.addReaction(emote).queue()
			}
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
