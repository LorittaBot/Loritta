package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.*
import net.dv8tion.jda.core.*
import net.dv8tion.jda.core.entities.*

class ChannelInfoCommand : AbstractCommand("channelinfo", listOf("channel"), CommandCategory.DISCORD) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["CHANNELINFO_Description"]
    }

    override fun canUseInPrivateChannel(): Boolean {
        return false
    }

    override fun run(context: CommandContext, locale: BaseLocale) {
        var channel = if (context.args.isEmpty()) {
            context.message.textChannel
        } else {
            if (context.guild.getTextChannelById(context.args[0]) != null)
                context.guild.getTextChannelById(context.args[0])
            else
                null
        }

        if (channel == null) {
            context.reply(
                    LoriReply(
                            message = "Canal não encontrado!",
                            mentionUser = true,
                            prefix = Constants.ERROR
                    )
            )
            return
        }

        val builder = EmbedBuilder()
        builder.apply {
            setAuthor(context.userHandle.name, null, context.userHandle.avatarUrl)
            setColor(Constants.DISCORD_BURPLE)

            setTitle("Informações do canal #${channel.name}")
            addField("Nome do canal", channel.name, true)
            addField("ID do canal", channel.id, true)
            addField("Data de criação", channel.creationTime.humanize(locale), true)
            addField("Tópico", if (channel.topic != null) channel.topic else "Não definido", true)
            addField("NSFW Ativado", if (channel.isNSFW) "Sim" else "Não", true)
        }

        context.sendMessage(context.userHandle.asMention, builder.build())
    }
}