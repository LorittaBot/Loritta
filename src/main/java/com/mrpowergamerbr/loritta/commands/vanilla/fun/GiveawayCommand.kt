package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.convertToEpochMillis
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.onResponseByAuthor
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.MessageEmbed
import java.time.Instant
import kotlin.concurrent.thread

class GiveawayCommand : CommandBase("giveaway", listOf("jogalonge")) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["GIVEAWAY_Description"]
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.FUN;
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_MANAGE)
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val createGiveaway = context.reply(
				LoriReply(
						message = "Qual será o nome do giveaway?",
						prefix = "\uD83E\uDD14"
				)
		)

		createGiveaway.onResponseByAuthor(context, {
			val reason = it.message.rawContent
			createGiveaway.delete().queue()

			val giveawayDescription = context.reply(
					LoriReply(
							message = "Qual será a descrição do giveaway?",
							prefix = "\uD83E\uDD14"
					)
			)

			giveawayDescription.onResponseByAuthor(context, {
				val description = it.message.rawContent
				giveawayDescription.delete().queue()

				val giveawayTime = context.reply(
						LoriReply(
								message = "Por enquanto tempo irá durar o giveaway?",
								prefix = "\uD83E\uDD14"
						)
				)

				giveawayTime.onResponseByAuthor(context, {
					val time = it.message.rawContent
					giveawayTime.delete().queue()

					val giveawayReaction = context.reply(
							LoriReply(
									message = "Qual emoji deverá ser usado nas reações?",
									prefix = "\uD83E\uDD14"
							)
					)

					giveawayReaction.onResponseByAuthor(context, {
						var reaction = it.message.rawContent

						if (reaction.startsWith("<")) {
							reaction = reaction.substring(2, reaction.length - 1)
						}
						giveawayReaction.delete().queue()

						val giveawayWhere = context.reply(
								LoriReply(
										message = "Em qual canal deverá acontecer o giveaway?",
										prefix = "\uD83E\uDD14"
								)
						)

						giveawayWhere.onResponseByAuthor(context, {
							val where = it.message.rawContent
							val epoch = time.convertToEpochMillis()

							try {
								giveawayWhere.addReaction(reaction).complete()
							} catch (e: Exception) {
								reaction = "\uD83C\uDF89"
							}

							giveawayWhere.delete().queue()

							val channel = it.guild.getTextChannelsByName(where, true)[0]

							val embed = createEmbed(reason, description, reaction, epoch)

							val message = channel.sendMessage(embed).complete()
							val messageId = message.id

							message.addReaction(reaction).complete()

							val giveaway = Giveaway(reason, description, 1, epoch, reaction, channel.id, messageId)

							thread {
								val config = loritta.getServerConfigForGuild(context.guild.id)

								config.giveaways.add(giveaway)

								loritta save config
							}
						})
					})
				})
			})
		})
	}

	companion object {
		fun createEmbed(reason: String, description: String, reaction: String, epoch: Long): MessageEmbed {
			val secondsRemaining = (epoch - System.currentTimeMillis()) / 1000
			var messageReaction = if (reaction.contains(":")) {
				"<:$reaction>"
			} else {
				reaction
			}

			val embed = EmbedBuilder().apply {
				setTitle("\uD83C\uDF81 $reason")
				setDescription("$description\n\nUse $messageReaction para entrar!\nTempo restante: **$secondsRemaining** segundos")
				setColor(Constants.DISCORD_BURPLE)
				setFooter("Acabará em", null)
				setTimestamp(Instant.ofEpochMilli(epoch))
			}

			return embed.build()
		}
	}

	class Giveaway(
			val reason: String,
			val description: String,
			val userCount: Int,
			val finishAt: Long,
			val reaction: String,
			val channelId: String,
			val messageId: String
	) {
		constructor() : this("???", "???", 1, 0L, "???", "???", "???")
	}
}