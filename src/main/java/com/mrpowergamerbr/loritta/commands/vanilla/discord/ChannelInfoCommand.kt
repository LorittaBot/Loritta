package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.*
import net.dv8tion.jda.core.*
import net.dv8tion.jda.core.entities.*

class ChannelInfoCommand : AbstractCommand("channelinfo", listOf("channel"), CommandCategory.DISCORD) {

    // Criado por MrGaabriel
    // TODO: Mudar as mensagens hardcoded para as das locales

    override fun getDescription(locale: BaseLocale): String {
        return locale["CHANNELINFO_Description"]
    }

    override fun canUseInPrivateChannel(): Boolean {
        return false
    }

    override fun run(context: CommandContext, locale: BaseLocale) {
        var channel: TextChannel? = null
        if (context.args.isEmpty()) {
            channel = context.message.textChannel
        } else {
            channel = if (context.guild.getTextChannelById(context.args[0]) != null) context.guild.getTextChannelById(context.args[0]) else null
        }

        if (channel == null) {
            context.reply(LoriReply(
                    message = "Canal não encontrado!",
                    mentionUser = true,
                    prefix = "<:erro:326509900115083266>"
            ))
            return
        }

        val builder = EmbedBuilder()
        builder.apply {
            setAuthor(context.userHandle.name, null, context.userHandle.avatarUrl)
            setColor(Constants.DISCORD_BURPLE)

            setTitle("Informações do canal #${channel.name}")
            addField("Nome do canal", channel.name, true)
            addField("ID do canal", channel.id, true)
            addField("Data de criação", channel.creationTime.humanize(), true)
            addField("Tópico", if (channel.topic != null) channel.topic else "Não definido", true)
            addField("NSFW Ativado", if (channel.isNSFW) "Sim" else "Não", true)
        }

        context.sendMessage(context.userHandle.asMention, builder.build())
    }
}