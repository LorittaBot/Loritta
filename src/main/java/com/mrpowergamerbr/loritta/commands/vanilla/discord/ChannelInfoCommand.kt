package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.DateUtils
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.TextChannel

class ChannelInfoCommand : AbstractCommand("channelinfo", listOf("channel"), CommandCategory.DISCORD) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.commands.channelInfo.description
	}
	
	override fun canUseInPrivateChannel(): Boolean {
		return false
	}
	
	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		val channel = if (context.args.isEmpty()) {
			context.message.textChannel
		} else {
			getTextChannel(context, context.rawArgs[0])
		}
		
		if (channel == null) {
			context.reply(
					LoriReply(
							message = locale.commands.channelInfo.channelNotFound,
							prefix = Constants.ERROR
					)
			)
			return
		}
		
		val channelCreatedDiff = DateUtils.formatDateDiff(channel.creationTime.toInstant().toEpochMilli(), context.locale)
		
		val builder = EmbedBuilder()
		
		builder.setColor(Constants.DISCORD_BLURPLE)
		builder.setTitle("\uD83D\uDC81 ${locale["CHANNELINFO_ChannelInformation", "#${channel.name}"]}")
		
		builder.addField("\uD83D\uDD39 ${context.locale.commands.channelInfo.channelMention}", channel.asMention, true)
		builder.addField("\uD83D\uDCBB ${context.locale.get("USERINFO_ID_DO_DISCORD")}", "`${channel.id}`", true)
		builder.addField("\uD83D\uDD1E NSFW", if (channel.isNSFW) locale["LORITTA_Yes"] else locale["LORITTA_No"], true)
		builder.addField("\uD83D\uDCC5 ${context.locale.commands.channelInfo.channelCreated}", channelCreatedDiff, true)
		builder.addField("\uD83D\uDCD8 ${context.locale.commands.channelInfo.channelTopic}", if (channel.topic.isNullOrEmpty()) context.locale.commands.channelInfo.undefined else "```${channel.topic}```", true)
		
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
