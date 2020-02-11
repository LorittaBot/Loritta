package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.DateUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.TextChannel
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.commands.notNull
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import kotlin.contracts.ExperimentalContracts

class ChannelInfoCommand : LorittaCommand(arrayOf("channelinfo", "channel"), category = CommandCategory.DISCORD) {
	
	override fun getDescription(locale: BaseLocale): String? {
		return locale["commands.discord.channelinfo.description"]
	}
	
	override val canUseInPrivateChannel = false
	
	@Subcommand
	@ExperimentalContracts
	suspend fun channelInfo(context: DiscordCommandContext, channel: TextChannel? = context.event.textChannel!!) {
		notNull(channel, context.locale["commands.discord.channelinfo.notFound"])
		
		val channelCreatedDiff = DateUtils.formatDateDiff(channel.timeCreated.toInstant().toEpochMilli(), context.legacyLocale)
		
		val builder = EmbedBuilder()
		
		builder.setColor(Constants.DISCORD_BLURPLE)
		builder.setTitle("\uD83D\uDC81 ${context.locale["commands.discord.channelinfo.channelInfo", "#${channel.name}"]}")
		
		builder.addField("\uD83D\uDD39 ${context.locale["commands.discord.channelinfo.channelMention"]}", channel.asMention, true)
		builder.addField("\uD83D\uDCBB ${context.legacyLocale.get("USERINFO_ID_DO_DISCORD")}", "`${channel.id}`", true)
		builder.addField("\uD83D\uDD1E NSFW", if (channel.isNSFW) context.legacyLocale["LORITTA_Yes"] else context.legacyLocale["LORITTA_No"], true)
		builder.addField("\uD83D\uDCC5 ${context.locale["commands.discord.channelinfo.channelCreated"]}", channelCreatedDiff, true)
		builder.addField("\uD83D\uDCD8 ${context.locale["commands.discord.channelinfo.channelTopic"]}", if (channel.topic.isNullOrEmpty()) context.locale["commands.discord.channelinfo.undefined"] else "```${channel.topic}```", true)
		builder.addField("\uD83D\uDD39 Guild", "`${channel.guild.name}`", true)
		
		context.sendMessage(context.userHandle.asMention, builder.build())
	}
}