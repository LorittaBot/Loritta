package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder

class ChannelInfoCommand : AbstractCommand("channelinfo", listOf("channel"), CommandCategory.DISCORD) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["CHANNELINFO_Description"]
    }

    override fun canUseInPrivateChannel(): Boolean {
        return false
    }

    override fun run(context: CommandContext, locale: BaseLocale) {
        val channel = if (context.args.isEmpty()) {
            context.message.textChannel
        } else {
            try {
                context.guild.getTextChannelById(context.args[0])
            } catch (e: Exception) {
                null
            }
        }

        if (channel == null) {
            context.reply(
                    LoriReply(
                            message = locale["CHANNELINFO_ChannelNotFound"],
                            prefix = Constants.ERROR
                    )
            )
            return
        }

        val builder = EmbedBuilder()
        builder.apply {
            setAuthor(context.userHandle.name, null, context.userHandle.avatarUrl)
            setColor(Constants.DISCORD_BLURPLE)
            setTitle(locale["CHANNELINFO_ChannelInformation", channel.name])
            addField(locale["DASHBOARD_ChannelName"], channel.name, true)
            addField(locale["CHANNELINFO_ChannelId"], channel.id, true)
            addField(locale["SERVERINFO_CREATED_IN"], channel.creationTime.humanize(locale), true)
            addField(locale["CHANNELINFO_Topic"], if (!channel.topic.isBlank()) channel.topic else locale["CHANNELINFO_Undefined"], true)
            addField(locale["CHANNELINFO_NsfwEnabled"], if (channel.isNSFW) locale["LORITTA_Yes"] else locale["LORITTA_No"], true)
        }

        context.sendMessage(context.userHandle.asMention, builder.build())
    }
}