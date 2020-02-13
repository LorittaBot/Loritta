package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Region
import net.perfectdreams.loritta.api.commands.CommandCategory

class ServerInfoCommand : AbstractCommand("serverinfo", listOf("guildinfo"), category = CommandCategory.DISCORD) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("SERVERINFO_DESCRIPTION")
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val embed = EmbedBuilder()

		var guild: JsonObject? = null

		if (context.rawArgs.isNotEmpty()) {
			val id = context.rawArgs.first()
			if (id.isValidSnowflake()) {
				guild = lorittaShards.queryGuildById(context.args[0])
			}
		} else {
			guild = lorittaShards.queryGuildById(context.guild.idLong)
		}

		if (guild == null) {
			context.reply(
					LoriReply(
							message = context.legacyLocale["SERVERINFO_UnknownGuild", context.args[0]],
							prefix = Constants.ERROR
					)
			)
			return
		}

		val iconUrl = guild["iconUrl"].nullString
		val splashUrl = guild["splashUrl"].nullString
		val name = guild["name"].string
		val id = guild["id"].string
		val shardId = guild["shardId"].int
		val ownerId = guild["ownerId"].string
		val region = Region.valueOf(guild["region"].string)
		val owner = lorittaShards.retrieveUserById(ownerId)
		val textChannelCount = guild["count"]["textChannels"].int
		val voiceChannelCount = guild["count"]["voiceChannels"].int
		val timeCreated = guild["timeCreated"].long
		val timeJoined = guild["timeJoined"].long
		val memberCount = guild["count"]["members"].int
		val onlineMembers = guild["count"]["onlineMembers"].int
		val idleMembers = guild["count"]["idleMembers"].int
		val doNotDisturbMembers = guild["count"]["doNotDisturbMembers"].int
		val offlineMembers = guild["count"]["offlineMembers"].int
		val bots = guild["count"]["bots"].int
		val users = memberCount - bots

		// Baseado no comando ?serverinfo do Dyno
		embed.setThumbnail(iconUrl) // Ícone da Guild
		embed.setImage(splashUrl?.replace("jpg", "png")?.plus("?size=2048")) // Background do Invite da Guild
		embed.setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)
		embed.setTitle("<:discord:314003252830011395> $name", null) // Nome da Guild
		embed.addField("💻 ID", id, true) // ID da Guild
		embed.addField("\uD83D\uDCBB Shard ID", "$shardId", true)
		embed.addField("👑 ${context.legacyLocale["SERVERINFO_OWNER"]}", "`${owner?.name}#${owner?.discriminator}` (${ownerId})", true) // Dono da Guild
		embed.addField("🌎 ${context.legacyLocale["SERVERINFO_REGION"]}", region.getName(), true) // Região da Guild
		embed.addField("\uD83D\uDCAC ${context.legacyLocale["SERVERINFO_CHANNELS"]} (${textChannelCount + voiceChannelCount})", "\uD83D\uDCDD **${locale["SERVERINFO_CHANNELS_TEXT"]}:** ${textChannelCount}\n\uD83D\uDDE3 **${locale["SERVERINFO_CHANNELS_VOICE"]}:** $voiceChannelCount", true) // Canais da Guild
		val createdAtDiff = DateUtils.formatDateDiff(timeCreated, locale)
		embed.addField("\uD83D\uDCC5 ${context.legacyLocale["SERVERINFO_CREATED_IN"]}", "${timeCreated.humanize(locale)} ($createdAtDiff)", true)
		val joinedAtDiff = DateUtils.formatDateDiff(timeJoined, locale)
		embed.addField("\uD83C\uDF1F ${context.legacyLocale["SERVERINFO_JOINED_IN"]}", "${timeJoined.humanize(locale)} ($joinedAtDiff)", true)
		embed.addField("👥 ${context.legacyLocale["SERVERINFO_MEMBERS"]} ($memberCount)", "<:online:313956277808005120> **${context.legacyLocale.get("SERVERINFO_ONLINE")}:** $onlineMembers |<:away:313956277220802560> **${context.legacyLocale.get("SERVERINFO_AWAY")}:** $idleMembers |<:dnd:313956276893646850> **${context.legacyLocale.get("SERVERINFO_BUSY")}:** $doNotDisturbMembers |<:offline:313956277237710868> **${context.legacyLocale.get("SERVERINFO_OFFLINE")}:** $offlineMembers\n\uD83D\uDE4B **${context.legacyLocale.get("SERVERINFO_PEOPLE")}:** $users\n\uD83E\uDD16 **${context.legacyLocale["SERVERINFO_BOTS"]}:** $bots", true) // Membros da Guild

		context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
	}
}
