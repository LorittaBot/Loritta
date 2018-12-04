package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.TextChannel

class ChannelInfoCommand : AbstractCommand("channelinfo", listOf("channel"), CommandCategory.DISCORD) {
    override fun getDescription(locale: BaseLocale): String {
        return locale["CHANNELINFO_Description"]
    }

    override fun canUseInPrivateChannel(): Boolean {
        return false
    }

    override suspend fun run(context: CommandContext,locale: BaseLocale) {
        val channel = if (context.args.isEmpty()) {
            context.message.textChannel
        } else {
			getTextChannel(context, context.rawArgs[0])
        }

		// TODO: Migrar para o sistema novo de locales + "embelezar" o comando
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
            addField(locale["CHANNELINFO_Topic"], if (!channel.topic.isNullOrBlank()) channel.topic else locale["CHANNELINFO_Undefined"], true)
            addField(locale["CHANNELINFO_NsfwEnabled"], if (channel.isNSFW) locale["LORITTA_Yes"] else locale["LORITTA_No"], true)
        }

        context.sendMessage(context.userHandle.asMention, builder.build())
    }
	
	fun getTextChannel(context: CommandContext, input: String?): TextChannel? {
		if (input == null)
			return null
		
		val guild = context.guild
		
		val channels = guild.getTextChannelsByName(input, false)
		if (channels.isNotEmpty()) {
			return channels[0]
		}
		
		val id = input
				.replace("<", "")
				.replace("#", "")
				.replace(">", "")
		
		if (!id.isValidSnowflake())
			return null
		
		val channel = guild.getTextChannelById(id)
		if (channel != null) {
			return channel
		}
		
		return null
	}
}
