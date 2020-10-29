package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply

class SayCommand : AbstractCommand("say", listOf("falar"), CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["$LOCALE_PREFIX.say.description"]
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments = arguments {
		argument(ArgumentType.TEXT) {
		}
	}

	override fun getExamples(locale: LegacyBaseLocale): List<String> {
		return locale.toNewLocale().getList("$LOCALE_PREFIX.say.examples")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_SERVER)
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		if (context.rawArgs.isNotEmpty()) {
			var args = context.rawArgs
			var currentIdx = 0

			val arg0 = context.rawArgs[0]
			var isEditMode = false
			var editMessage: Message? = null

			if (arg0 == "edit" || arg0 == "editar") {
				isEditMode = true
				currentIdx++
			}

			val channelIdOrMessageLink = context.rawArgs[currentIdx]

			if (isEditMode) {
				val split = channelIdOrMessageLink.split("/")

				if (split.size >= 2) {
					val messageId = split.last()
					val channelId = split.dropLast(1).last()

					editMessage = context.guild.getTextChannelById(channelId)!!
							.retrieveMessageById(messageId)
							.await()
					args = args.remove(0) // Removes the "edit"
					args = args.remove(0) // Removes the message URL
				} else { return } // TODO: Good message
			}

			// Pegando canal de texto, via menções, ID ou nada
			val channel = if (isEditMode) editMessage!!.channel else if (args.size >= 2) {
				if (channelIdOrMessageLink.startsWith("<#") && channelIdOrMessageLink.endsWith(">")) {
					try {
						val ch = context.guild.getTextChannelById(channelIdOrMessageLink.substring(2, channelIdOrMessageLink.length - 1))
						args = args.remove(0)
						ch
					} catch (e: Exception) {
						null
					}
				} else {
					try {
						val ch = context.guild.getTextChannelById(channelIdOrMessageLink)
						args = args.remove(0)
						ch
					} catch (e: Exception) {
						null
					}
				}
			} else { null } ?: context.event.channel

			if (channel is TextChannel) { // Caso seja text channel...
				if (!channel.canTalk()) {
					context.reply(
                            LorittaReply(
                                    context.locale["$LOCALE_PREFIX.say.iDontHavePermissionToTalkIn", channel.asMention],
                                    Constants.ERROR
                            )
					)
					return
				}
				if (!channel.canTalk(context.handle)) {
					context.reply(
                            LorittaReply(
									context.locale["$LOCALE_PREFIX.say.youDontHavePermissionToTalkIn", channel.asMention],
                                    Constants.ERROR
                            )
					)
					return
				}
				if (context.config.blacklistedChannels.contains(channel.idLong) && !context.lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
					context.reply(
                            LorittaReply(
									context.locale["$LOCALE_PREFIX.say.commandsCannotBeUsedIn", channel.asMention],
                                    Constants.ERROR
                            )
					)
					return
				}
			}

			var message = args.joinToString(" ")

			if (!context.isPrivateChannel && !context.handle.hasPermission(channel as TextChannel, Permission.MESSAGE_MENTION_EVERYONE))
				message = message.escapeMentions()

			// Watermarks the message to "deanonymise" the message, to avoid users reporting Loritta for ToS breaking stuff, even tho it was
			// a malicious user sending the messages.
			val watermarkedMessage = MessageUtils.watermarkMessage(
					message,
					context.userHandle,
					context.locale["$LOCALE_PREFIX.say.messageSentBy"]
			)

			val discordMessage = try {
				MessageUtils.generateMessage(
						watermarkedMessage,
						listOf(context.guild, context.userHandle),
						context.guild
				)
			} catch (e: Exception) {
				null
			}

			if (discordMessage != null)
				(
						if (isEditMode)
							editMessage!!.editMessage(discordMessage)
						else
							channel.sendMessage(discordMessage)
						).queue()
			else
				(
						if (isEditMode)
							editMessage!!.editMessage(message)
						else
							channel.sendMessage(message)
						).queue()

			if (context.event.channel != channel && channel is TextChannel)
				context.reply(
                        LorittaReply(
                                context.locale["$LOCALE_PREFIX.say.messageSuccessfullySent", channel.asMention],
                                "\uD83C\uDF89"
                        )
				)

		} else {
			this.explain(context)
		}
	}

	companion object {
		const val LOCALE_PREFIX = "commands.moderation"
	}

}