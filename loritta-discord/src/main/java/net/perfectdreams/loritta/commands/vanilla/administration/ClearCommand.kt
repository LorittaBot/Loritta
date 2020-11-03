package net.perfectdreams.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase

class ClearCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("clean", "limpar", "clear"), CommandCategory.ADMIN) {

    override fun command(): Command<CommandContext> = create {
        localizedDescription("commands.moderation.clear.description")
        localizedExamples("commands.moderation.clear.examples")

        userRequiredPermissions = listOf(Permission.MESSAGE_MANAGE)
        botRequiredPermissions = listOf(Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY)

        usage {
            argument(ArgumentType.NUMBER) {
                optional = false
            }
            argument(ArgumentType.USER){
                optional = true
            }
        }

        executesDiscord {
            if (args.isEmpty()) return@executesDiscord explain()

            val count = args[0].toIntOrNull()
            val channel = discordMessage.channel as? TextChannel ?: return@executesDiscord

            val target = user(1)

            if (count == null || count !in 2..100)
                fail(locale["commands.moderation.clear.invalidClearRange"], Constants.ERROR)

            message.runCatching {
                delete()
            }

            val messages = channel.history.retrievePast(count).await()

            val disallowedMessages = messages.filter { ((((System.currentTimeMillis() / 1000) - it.timeCreated.toEpochSecond()) > 1209600) || (it.isPinned)) && if (target != null) it.author.idLong == target.id else true }
            val allowedMessages = messages - disallowedMessages

            if (allowedMessages.isEmpty())
                fail(locale["commands.moderation.clear.couldNotFindMessages"], Constants.ERROR)
            else clear(channel, messages)

            val replies = mutableListOf(LorittaReply(locale["commands.moderation.clear.success", user.asMention], "\uD83E\uDD73"))

            if (disallowedMessages.isNotEmpty())
                replies.add(LorittaReply(
                        locale["commands.moderation.clear.ignoredMessages", disallowedMessages.size],
                        "\uD83D\uDD37",
                        mentionUser = false
                ))

            reply(replies)
        }
    }

    suspend fun clear(channel: TextChannel, messages: List<Message>) {
        val allowedMessages = messages - messages.filter { (((System.currentTimeMillis() / 1000) - it.timeCreated.toEpochSecond() > 14 * 24 * 60 * 60) || (it.isPinned)) && it.channel.idLong == channel.idLong }

        channel.deleteMessages(allowedMessages).await()
    }

}