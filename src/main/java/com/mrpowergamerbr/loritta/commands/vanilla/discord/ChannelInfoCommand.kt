package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.*
import net.dv8tion.jda.core.*
import net.dv8tion.jda.core.entities.*

class ChannelInfoCommand : AbstractCommand("channelinfo", listOf("channel"), CommandCategory.DISCORD) {

    // Criado por MrGaabriel

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
            // aquela gambiarra que você respeita
            try {
                channel = if (context.guild.getTextChannelById(context.args[0]) != null) context.guild.getTextChannelById(context.args[0]) else null
            } catch (exception: NumberFormatException) {
                // ok, provavelmente o usuário não colocou o ID do canal, vamos checar os nomes dos canais!
                channel = context.guild.textChannels.filter { it.name == context.args[0] }.get(0)
            }
        }

        if (channel == null) {
            context.reply(LoriReply(
                    message = locale["CHANNELINFO_ChannelNotFound"],
                    mentionUser = true,
                    prefix = "<:erro:326509900115083266>"
            ))
            return
        }

        val builder = EmbedBuilder()
        builder.apply {
            setAuthor(context.userHandle.name, null, context.userHandle.avatarUrl)
            setColor(Constants.DISCORD_BURPLE)

            setTitle("${locale["CHANNELINFO_ChannelInfo"]} #${channel.name}")
            addField("${locale["CHANNELINFO_ChannelName"]}", channel.name, true)
            addField("${locale["CHANNELINFO_ChannelID"]}", channel.id, true)
            addField("${locale["CHANNELINFO_ChannelCreationTime"]}", channel.creationTime.humanize(), true)
            addField("${locale["CHANNELINFO_Topic"]}", if (!channel.topic.isEmpty()) channel.topic else locale["CHANNELINFO_TopicUndefined"], true)
            addField("${locale["CHANNELINFO_NsfwActivated"]}", if (channel.isNSFW) locale["LORITTA_Yes"] else locale["LORITTA_No"], true)
        }

        context.sendMessage(context.userHandle.asMention, builder.build())
    }
}