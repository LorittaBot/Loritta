package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Emote
import net.dv8tion.jda.core.entities.Icon
import net.dv8tion.jda.core.entities.Message
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class EmojiSearchCommand : AbstractCommand("emojisearch", listOf("procuraremoji")) {
	override fun getUsage(): String {
		return "query"
	}

	override fun getExample(): List<String> {
		return listOf("think")
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["EMOJISEARCH_Description"]
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val query = context.args.joinToString(" ").toLowerCase()

			if (3 > query.length) {
				context.reply(
						LoriReply(
								locale["EMOJISEARCH_SmallQuery"],
								Constants.ERROR
						)
				)
				return
			}

			var queriedEmotes = mutableListOf<Emote>()

			for (guild in lorittaShards.getGuilds()) {
				val emotes = guild.emotes.filter { it.name.toLowerCase().contains(query) }

				queriedEmotes.addAll(emotes)
			}

			queriedEmotes = queriedEmotes.sortedByDescending { it.guild.members.size }.toMutableList()

			val message = sendQueriedEmbed(context, queriedEmotes, query, 0)
		} else {
			context.explain()
		}
	}

	fun sendQueriedEmbed(context: CommandContext, _queriedEmotes: List<Emote>, query: String, page: Int): Message {
		val emotesPreview = BufferedImage(333, 128, BufferedImage.TYPE_INT_ARGB)
		val graphics = emotesPreview.graphics

		val queriedEmotes = _queriedEmotes.subList(page * 9, Math.min(_queriedEmotes.size, (page + 1) * 9))
		var x = 0
		var y = 0
		for ((index, emote) in queriedEmotes.withIndex()) {
			val url = emote.imageUrl
			val emoteImage = LorittaUtils.downloadImage(url).getScaledInstance(64, 64, BufferedImage.SCALE_SMOOTH)

			if (x + 64 > 333) {
				x = 32
				y += 64
			}
			graphics.drawImage(emoteImage, x, y, null)
			x += 64
		}

		val embed = EmbedBuilder().apply {
			setTitle("<:osama:325332212255948802> ${context.locale["EMOJISEARCH_Title"]}")
			setDescription(context.locale["EMOJISEARCH_Results", _queriedEmotes.size, query])
			setColor(Constants.DISCORD_BURPLE)
			setImage("attachment://emotes.png")
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

				val embed = EmbedBuilder().apply {
					setTitle("${emote.asMention} ${emote.name}")
					setThumbnail(emote.imageUrl)
					setDescription(context.locale["EMOJISEARCH_FoundAt", emote.guild.name] + "\n\n[Download](${emote.imageUrl})")
					setColor(Constants.DISCORD_BURPLE)
				}

				val emoteInfo = context.sendMessage(embed.build())

				if (context.guild.selfMember.hasPermission(Permission.MANAGE_EMOTES) && context.handle.hasPermission(Permission.MANAGE_EMOTES)) {
					emoteInfo.addReaction("wumplus:388417805126467594").complete()

					emoteInfo.onReactionAddByAuthor(context) {
						if (it.reactionEmote.name == "wumplus") {
							emoteInfo.delete().queue()
							try {
								val os = ByteArrayOutputStream()
								try {
									ImageIO.write(LorittaUtils.downloadImage(emote.imageUrl), "png", os)
								} catch (e: Exception) {
								}

								val inputStream = ByteArrayInputStream(os.toByteArray())

								val emote = context.guild.controller.createEmote(emote.name, Icon.from(inputStream)).complete()

								context.reply(
										LoriReply(
												context.locale["EMOJISEARCH_AddSuccess"],
												emote.asMention
										)
								)
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
				message.addReaction(emote).complete()
			}
		}

		if (page > 0) {
			message.addReaction("⏪").complete()
		}
		if (queriedEmotes.size >= 9) {
			message.addReaction("⏩").complete()
		}

		return message
	}
}
