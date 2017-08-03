package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.humanize
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.OnlineStatus
import java.awt.Color

class ServerInfoCommand : CommandBase() {
	override fun getDescription(locale: BaseLocale): String {
		return locale.SERVERINFO_DESCRIPTION.msgFormat()
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.DISCORD
	}

	override fun getLabel(): String {
		return "serverinfo"
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext) {
		val embed = EmbedBuilder()

		// Baseado no comando ?serverinfo do Dyno

		embed.setThumbnail(context.guild.iconUrl) // Ícone da Guild
		embed.setColor(Color(114, 137, 218)) // Cor do embed (Cor padrão do Discord)
		embed.setTitle("<:discord:314003252830011395> ${context.guild.name}", null) // Nome da Guild
		embed.addField("💻 ID", context.guild.id, true) // ID da Guild
		embed.addField("👑 ${context.locale.SERVERINFO_OWNER.msgFormat()}", context.guild.owner.asMention, true) // Dono da Guild
		embed.addField("🌎 ${context.locale.SERVERINFO_REGION.msgFormat()}", context.guild.region.getName(), true) // Região da Guild
		embed.addField("\uD83D\uDCAC ${context.locale.SERVERINFO_CHANNELS.msgFormat()}", "\uD83D\uDCDD **${context.locale.SERVERINFO_CHANNELS_TEXT.msgFormat()}:** ${context.guild.textChannels.size}\n\uD83D\uDDE3 **${context.locale.SERVERINFO_CHANNELS_VOICE.msgFormat()}:** ${context.guild.voiceChannels.size}", true) // Canais da Guild
		embed.addField("\uD83D\uDCC5 ${context.locale.SERVERINFO_CREATED_IN.msgFormat()}", context.guild.creationTime.humanize(), true)
		embed.addField("\uD83C\uDF1F ${context.locale.SERVERINFO_JOINED_IN.msgFormat()}", context.guild.selfMember.joinDate.humanize(), true)
		embed.addField("👥 ${context.locale.SERVERINFO_MEMBERS.msgFormat()} (${context.guild.members.size})", "<:online:313956277808005120> **${context.locale.SERVERINFO_ONLINE.msgFormat()}:** ${context.guild.members.filter{ it.onlineStatus == OnlineStatus.ONLINE }.size} |<:away:313956277220802560> **${context.locale.SERVERINFO_AWAY.msgFormat()}:** ${context.guild.members.filter { it.onlineStatus == OnlineStatus.IDLE }.size} |<:dnd:313956276893646850> **${context.locale.SERVERINFO_BUSY.msgFormat()}:** ${context.guild.members.filter { it.onlineStatus == OnlineStatus.DO_NOT_DISTURB }.size} |<:offline:313956277237710868> **${context.locale.SERVERINFO_OFFLINE.msgFormat()}:** ${context.guild.members.filter { it.onlineStatus == OnlineStatus.OFFLINE }.size}\n\uD83D\uDE4B **${context.locale.SERVERINFO_PEOPLE.msgFormat()}:** ${context.guild.members.filter{ !it.user.isBot }.size}\n\uD83E\uDD16 **${context.locale.SERVERINFO_BOTS.msgFormat()}:** ${context.guild.members.filter{ it.user.isBot }.size}", true) // Membros da Guild
		embed.setThumbnail(context.guild.iconUrl)

		/* var roles = "";
		var roleList = ArrayList<String>()

		for (role in context.guild.roles) {
			var preview = roles + role.asMention
			if (preview.length > 1023) {
				roleList.add(roles);
				roles = "";
			}
			roles += role.asMention + ", ";
		}

		roleList.add(roles)

		embed.addField("\uD83D\uDCBC ${context.locale.SERVERINFO_ROLES.msgFormat()} (${context.guild.roles.size})", roleList[0], false) // Cargos da Guild

		for (i in 1..roleList.size - 1) {
			embed.addField("", roleList[i], false);
		}

		var emotes = "";
		var emotesList = ArrayList<String>()

		for (emote in context.guild.emotes) {
			var preview = emotes + emote.asMention
			if (preview.length > 1023) {
				emotesList.add(emotes);
				emotes = "";
			}
			emotes += emote.asMention;
		}

		emotesList.add(emotes)

		embed.addField("<:osama:325332212255948802> ${context.locale.SERVERINFO_CUSTOM_EMOJIS.msgFormat()} (${context.guild.emotes.size})", emotesList[0], false);

		for (i in 1..emotesList.size - 1) {
			embed.addField("", emotesList[i], false);
		} */

		context.sendMessage(embed.build()) // phew, agora finalmente poderemos enviar o embed!
	}
}