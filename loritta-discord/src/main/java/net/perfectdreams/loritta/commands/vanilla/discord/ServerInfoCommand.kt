package net.perfectdreams.loritta.commands.vanilla.discord

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.DateUtils
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Region
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.DiscordUtils

class ServerInfoCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("serverinfo", "guildinfo"), CommandCategory.DISCORD) {
	companion object {
		private const val LOCALE_PREFIX = "commands.discord.serverinfo"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.description")

		canUseInPrivateChannel = false

		executesDiscord {
			val context = this

			val embed = EmbedBuilder()

			var guild: JsonObject? = null

			var guildId = context.guild.idLong

			if (context.args.isNotEmpty()) {
				val id = context.args.first()
				if (id.isValidSnowflake()) {
					guildId = id.toLong()
					guild = lorittaShards.queryGuildById(context.args[0])
				}
			} else {
				guild = lorittaShards.queryGuildById(context.guild.idLong)
			}

			if (guild == null) {
				context.reply(
						LorittaReply(
								context.locale["commands.guildDoesNotExist", guildId],
								Constants.ERROR
						)
				)
				return@executesDiscord
			}

			val iconUrl = guild["iconUrl"].nullString
			val splashUrl = guild["splashUrl"].nullString
			val name = guild["name"].string
			val id = guild["id"].string
			val shardId = guild["shardId"].int
			val cluster = DiscordUtils.getLorittaClusterForGuildId(id.toLong())
			val ownerId = guild["ownerId"].string
			val region = Region.valueOf(guild["region"].string)
			val owner = lorittaShards.retrieveUserInfoById(ownerId.toLong())
			val textChannelCount = guild["count"]["textChannels"].int
			val voiceChannelCount = guild["count"]["voiceChannels"].int
			val timeCreated = guild["timeCreated"].long
			val timeJoined = guild["timeJoined"].long
			val memberCount = guild["count"]["members"].int

			// Baseado no comando ?serverinfo do Dyno
			embed.setThumbnail(iconUrl) // Ícone da Guild
			embed.setImage(splashUrl?.replace("jpg", "png")?.plus("?size=2048")) // Background do Invite da Guild
			embed.setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)
			embed.setTitle("<:discord:314003252830011395> $name", null) // Nome da Guild
			embed.addField("💻 ID", id, true) // ID da Guild
			embed.addField("\uD83D\uDCBB Shard ID", "$shardId — Loritta Cluster ${cluster.id} (`${cluster.name}`)", true)
			embed.addField("👑 ${context.locale["$LOCALE_PREFIX.owner"]}", "`${owner?.name}#${owner?.discriminator}` (${ownerId})", true) // Dono da Guild
			embed.addField("🌎 ${context.locale["$LOCALE_PREFIX.region"]}", region.getName(), true) // Região da Guild
			embed.addField("\uD83D\uDCAC ${context.locale["$LOCALE_PREFIX.channels"]} (${textChannelCount + voiceChannelCount})", "\uD83D\uDCDD **${locale["$LOCALE_PREFIX.textChannels"]}:** ${textChannelCount}\n\uD83D\uDDE3 **${locale["$LOCALE_PREFIX.voiceChannels"]}:** $voiceChannelCount", true) // Canais da Guild
			val createdAtDiff = DateUtils.formatDateDiff(timeCreated, locale)
			embed.addField("\uD83D\uDCC5 ${context.locale["$LOCALE_PREFIX.createdAt"]}", "${timeCreated.humanize(locale)} ($createdAtDiff)", true)
			val joinedAtDiff = DateUtils.formatDateDiff(timeJoined, locale)
			embed.addField("\uD83C\uDF1F ${context.locale["$LOCALE_PREFIX.joinedAt"]}", "${timeJoined.humanize(locale)} ($joinedAtDiff)", true)
			embed.addField("👥 ${context.locale["$LOCALE_PREFIX.members"]} ($memberCount)", "", true) // Membros da Guild

			context.sendMessage(context.getUserMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
		}
	}
}