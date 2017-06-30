package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.humanize
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.OnlineStatus
import java.awt.Color

class ServerInfoCommand : CommandBase() {
	override fun getDescription(): String {
		return "Veja as informações do servidor do Discord atual!"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.DISCORD
	}

	override fun getLabel(): String {
		return "serverinfo"
	}

	override fun run(context: CommandContext) {
		val embed = EmbedBuilder()

		// Baseado no comando ?serverinfo do Dyno

		embed.setThumbnail(context.guild.iconUrl) // Ícone da Guild
		embed.setColor(Color(114, 137, 218)) // Cor do embed (Cor padrão do Discord)
		embed.setTitle("<:discord:314003252830011395> ${context.guild.name}", null) // Nome da Guild
		embed.addField("💻 ID", context.guild.id, true) // ID da Guild
		embed.addField("👑 Dono", context.guild.owner.asMention, true) // Dono da Guild
		embed.addField("🌎 Região", context.guild.region.getName(), true) // Região da Guild
		embed.addField("\uD83D\uDCAC Canais", "\uD83D\uDCDD **Texto:** ${context.guild.textChannels.size}\n\uD83D\uDDE3 **Voz:** ${context.guild.voiceChannels.size}", true) // Canais da Guild
		embed.addField("\uD83D\uDCC5 Criado em", context.guild.creationTime.humanize(), true)
		embed.addField("\uD83C\uDF1F Entrei aqui em", context.guild.selfMember.joinDate.humanize(), true)
		embed.addField("👥 Membros (${context.guild.members.size})", "<:online:313956277808005120> **Online:** ${context.guild.members.filter{ it.onlineStatus == OnlineStatus.ONLINE }.size} |<:away:313956277220802560> **Ausente:** ${context.guild.members.filter { it.onlineStatus == OnlineStatus.IDLE }.size} |<:dnd:313956276893646850> **Ocupado:** ${context.guild.members.filter { it.onlineStatus == OnlineStatus.DO_NOT_DISTURB }.size} |<:offline:313956277237710868> **Offline:** ${context.guild.members.filter { it.onlineStatus == OnlineStatus.OFFLINE }.size}\n\uD83D\uDE4B **Pessoas:** ${context.guild.members.filter{ !it.user.isBot }.size}\n\uD83E\uDD16 **Bots:** ${context.guild.members.filter{ it.user.isBot }.size}", true) // Membros da Guild
		embed.setThumbnail(context.guild.iconUrl)

		var roles = "";
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

		embed.addField("\uD83D\uDCBC Cargos (${context.guild.roles.size})", roleList[0], false) // Cargos da Guild

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

		embed.addField("<:osama:325332212255948802> Emojis customizados (${context.guild.emotes.size})", emotesList[0], false);

		for (i in 1..emotesList.size - 1) {
			embed.addField("", emotesList[i], false);
		}

		context.sendMessage(embed.build()) // phew, agora finalmente poderemos enviar o embed!
	}
}