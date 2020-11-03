package net.perfectdreams.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.vanilla.administration.SayCommand
import com.mrpowergamerbr.loritta.utils.*
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.commands.getTextChannel

class SayCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("say", "falar"), CommandCategory.ADMIN) {

    override fun command(): Command<CommandContext> = create {
        localizedDescription("commands.moderation.say.description")
        localizedExamples("commands.moderation.say.examples")

        usage {
            argument(ArgumentType.TEXT) {
                optional = false
            }
        }

        executesDiscord {
            if (args.isEmpty()) return@executesDiscord explain()

            var args = args
            var channel: MessageChannel? = getTextChannel(args[0])

            if (channel != null)
                args = args.drop(1)
            else {
                channel = discordMessage.channel
            }

            if (channel is TextChannel) { // Caso seja text channel...
                if (!channel.canTalk()) {
                    fail(
                            locale["${SayCommand.LOCALE_PREFIX}.say.iDontHavePermissionToTalkIn", channel.asMention],
                            Constants.ERROR
                    )
                }
                if (!channel.canTalk(member!!)) {
                    fail(
                            locale["${SayCommand.LOCALE_PREFIX}.say.youDontHavePermissionToTalkIn", channel.asMention],
                            Constants.ERROR
                    )
                }
                if (serverConfig.blacklistedChannels.contains(channel.idLong) && !lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
                    fail(
                            locale["${SayCommand.LOCALE_PREFIX}.say.commandsCannotBeUsedIn", channel.asMention],
                            Constants.ERROR
                    )
                }
            }

            var text = args.joinToString(" ")

            if (!isPrivateChannel && member?.hasPermission(channel as TextChannel, Permission.MESSAGE_MENTION_EVERYONE) == true)
                text = text.escapeMentions()

            // Watermarks the message to "deanonymise" the message, to avoid users reporting Loritta for ToS breaking stuff, even tho it was
            // a malicious user sending the messages.
            val watermarkedMessage = MessageUtils.watermarkMessage(
                    text,
                    user,
                    locale["${SayCommand.LOCALE_PREFIX}.say.messageSentBy"]
            )

            val preparedMessage = runCatching {
                MessageUtils.generateMessage(
                        watermarkedMessage,
                        listOf(guild, user),
                        guild
                )
            }.getOrNull() ?: run {
                return@executesDiscord channel.sendMessage(text).queue()
            }

            channel.sendMessage(preparedMessage).queue()
        }
    }
}