package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.humanize
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.LORITTA_SHARDS
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.OnlineStatus

class ServerInfoCommand : CommandBase("serverinfo") {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("SERVERINFO_DESCRIPTION")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.DISCORD
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val embed = EmbedBuilder()

		var guild = context.guild
		val id = if (context.args.isNotEmpty()) { context.args[0] } else { null }

		if (id != null && id.isValidSnowflake()) {
			guild = LORITTA_SHARDS.getGuildById(context.args[0])
		}

		if (guild == null) {
			context.reply(
					LoriReply(
							message = context.locale["SERVERINFO_UnknownGuild", context.args[0]],
							prefix = Constants.ERROR
					)
			)
			return
		}

		// Baseado no comando ?serverinfo do Dyno
		embed.setThumbnail(guild.iconUrl) // Ícone da Guild
		embed.setColor(Constants.DISCORD_BURPLE) // Cor do embed (Cor padrão do Discord)
		embed.setTitle("<:discord:314003252830011395> ${guild.name}", null) // Nome da Guild
		embed.addField("💻 ID", guild.id, true) // ID da Guild
		embed.addField("👑 ${context.locale["SERVERINFO_OWNER"]}", guild.owner.asMention, true) // Dono da Guild
		embed.addField("🌎 ${context.locale["SERVERINFO_REGION"]}", guild.region.getName(), true) // Região da Guild
		embed.addField("\uD83D\uDCAC ${context.locale["SERVERINFO_CHANNELS"]}", "\uD83D\uDCDD **${context.locale.SERVERINFO_CHANNELS_TEXT.msgFormat()}:** ${guild.textChannels.size}\n\uD83D\uDDE3 **${context.locale.SERVERINFO_CHANNELS_VOICE.msgFormat()}:** ${guild.voiceChannels.size}", true) // Canais da Guild
		embed.addField("\uD83D\uDCC5 ${context.locale["SERVERINFO_CREATED_IN"]}", guild.creationTime.humanize(), true)
		embed.addField("\uD83C\uDF1F ${context.locale["SERVERINFO_JOINED_IN"]}", guild.selfMember.joinDate.humanize(), true)
		embed.addField("👥 ${context.locale["SERVERINFO_MEMBERS"]} (${guild.members.size})", "<:online:313956277808005120> **${context.locale.get("SERVERINFO_ONLINE")}:** ${guild.members.filter{ it.onlineStatus == OnlineStatus.ONLINE }.size} |<:away:313956277220802560> **${context.locale.get("SERVERINFO_AWAY")}:** ${guild.members.filter { it.onlineStatus == OnlineStatus.IDLE }.size} |<:dnd:313956276893646850> **${context.locale.get("SERVERINFO_BUSY")}:** ${guild.members.filter { it.onlineStatus == OnlineStatus.DO_NOT_DISTURB }.size} |<:offline:313956277237710868> **${context.locale.get("SERVERINFO_OFFLINE")}:** ${guild.members.filter { it.onlineStatus == OnlineStatus.OFFLINE }.size}\n\uD83D\uDE4B **${context.locale.get("SERVERINFO_PEOPLE")}:** ${guild.members.filter{ !it.user.isBot }.size}\n\uD83E\uDD16 **${context.locale.get("SERVERINFO_BOTS")}:** ${guild.members.filter{ it.user.isBot }.size}", true) // Membros da Guild
		embed.setThumbnail(guild.iconUrl)

		context.sendMessage(embed.build()) // phew, agora finalmente poderemos enviar o embed!
	}
}