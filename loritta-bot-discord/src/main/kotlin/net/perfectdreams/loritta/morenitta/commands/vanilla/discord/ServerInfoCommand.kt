package net.perfectdreams.loritta.morenitta.commands.vanilla.discord

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.Gender
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake

class ServerInfoCommand(loritta: LorittaBot) : AbstractCommand(loritta, "serverinfo", listOf("guildinfo"), category = net.perfectdreams.loritta.common.commands.CommandCategory.DISCORD) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.serverinfo.description")

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val embed = EmbedBuilder()

		var guild: JsonObject? = null

		if (context.rawArgs.isNotEmpty()) {
			val id = context.rawArgs.first()
			if (id.isValidSnowflake()) {
				guild = loritta.lorittaShards.queryGuildById(context.args[0])
			}
		} else {
			guild = loritta.lorittaShards.queryGuildById(context.guild.idLong)
		}

		if (guild == null) {
			context.reply(
                    LorittaReply(
                            message = context.locale["commands.command.serverinfo.unknownGuild", context.args[0]],
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
		val cluster = DiscordUtils.getLorittaClusterForGuildId(loritta, id.toLong())
		val ownerId = guild["ownerId"].string
		HarmonyLoggerFactory.logger {}.value.info { "ServerInfoCommand#retrieveUserInfoById - UserId: $ownerId" }
		val owner = loritta.lorittaShards.retrieveUserInfoById(ownerId.toLong())
		val ownerProfile = loritta.getLorittaProfile(ownerId.toLong())
		val ownerGender = loritta.newSuspendedTransaction { ownerProfile?.settings?.gender ?: Gender.UNKNOWN }
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
		embed.addField("👑 ${if (ownerGender != Gender.FEMALE) context.locale["commands.command.serverinfo.owner"] else context.locale["commands.command.serverinfo.ownerFemale"]}", "`${owner?.name}#${owner?.discriminator}` (${ownerId})", true) // Dono da Guild
		embed.addField("\uD83D\uDCAC ${context.locale["commands.command.serverinfo.channels"]} (${textChannelCount + voiceChannelCount})", "\uD83D\uDCDD **${locale["commands.command.serverinfo.textChannels"]}:** ${textChannelCount}\n\uD83D\uDDE3 **${locale["commands.command.serverinfo.voiceChannels"]}:** $voiceChannelCount", true) // Canais da Guild
		embed.addField("\uD83D\uDCC5 ${context.locale["commands.command.serverinfo.createdAt"]}", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(timeCreated), true)
		embed.addField("\uD83C\uDF1F ${context.locale["commands.command.serverinfo.joinedAt"]}", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifferenceWithDiscordMarkdown(timeJoined), true)
		embed.addField("👥 ${context.locale["commands.command.serverinfo.members"]} ($memberCount)", "", true) // Membros da Guild

		context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
	}
}