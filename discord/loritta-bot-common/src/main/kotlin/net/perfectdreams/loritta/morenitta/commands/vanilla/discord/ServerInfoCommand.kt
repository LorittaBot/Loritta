package net.perfectdreams.loritta.morenitta.commands.vanilla.discord

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.DateUtils
import net.perfectdreams.loritta.morenitta.utils.isValidSnowflake
import net.perfectdreams.loritta.morenitta.utils.locale.Gender
import net.perfectdreams.loritta.deviousfun.EmbedBuilder
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot

class ServerInfoCommand(loritta: LorittaBot) : AbstractCommand(loritta, "serverinfo", listOf("guildinfo"), category = net.perfectdreams.loritta.common.commands.CommandCategory.DISCORD) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.serverinfo.description")

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val embed = EmbedBuilder()

		var guild: Guild? = null

		if (context.rawArgs.isNotEmpty()) {
			val id = context.rawArgs.first()
			if (id.isValidSnowflake()) {
				guild = loritta.deviousFun.retrieveGuildOrNullById(Snowflake(context.args[0]))
			}
		} else {
			guild = loritta.deviousFun.retrieveGuildOrNullById(context.guild.idSnowflake)
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

		val iconUrl = guild.iconUrl
		val splashUrl = guild.splashUrl
		val name = guild.name
		val id = guild.id
		val shardId = DiscordUtils.getShardIdFromGuildId(loritta, guild.idLong)
		val cluster = DiscordUtils.getLorittaClusterForGuildId(loritta, id.toLong())
		val ownerId = guild.ownerId
		val owner = loritta.lorittaShards.retrieveUserInfoById(ownerId.toLong())
		val ownerProfile = loritta.getLorittaProfileAsync(ownerId.toLong())
		val ownerGender = loritta.newSuspendedTransaction { ownerProfile?.settings?.gender ?: Gender.UNKNOWN }
		val textChannelCount = guild.channels.count { it.type == ChannelType.GuildText }
		val voiceChannelCount = guild.channels.count { it.type == ChannelType.GuildVoice }
		val timeCreated = guild.timeCreated
		val timeJoined = guild.retrieveSelfMember().timeJoined
		val memberCount: Int? = null // guild["count"]["members"].int // TODO - DeviousFun

		// Baseado no comando ?serverinfo do Dyno
		embed.setThumbnail(iconUrl) // Ícone da Guild
		embed.setImage(splashUrl?.replace("jpg", "png")?.plus("?size=2048")) // Background do Invite da Guild
		embed.setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)
		embed.setTitle("<:discord:314003252830011395> $name", null) // Nome da Guild
		embed.addField("💻 ID", id, true) // ID da Guild
		embed.addField("\uD83D\uDCBB Shard ID", "$shardId — Loritta Cluster ${cluster.id} (`${cluster.name}`)", true)
		embed.addField("👑 ${if (ownerGender != Gender.FEMALE) context.locale["commands.command.serverinfo.owner"] else context.locale["commands.command.serverinfo.ownerFemale"]}", "`${owner?.name}#${owner?.discriminator}` (${ownerId})", true) // Dono da Guild
		embed.addField("\uD83D\uDCAC ${context.locale["commands.command.serverinfo.channels"]} (${textChannelCount + voiceChannelCount})", "\uD83D\uDCDD **${locale["commands.command.serverinfo.textChannels"]}:** ${textChannelCount}\n\uD83D\uDDE3 **${locale["commands.command.serverinfo.voiceChannels"]}:** $voiceChannelCount", true) // Canais da Guild
		embed.addField("\uD83D\uDCC5 ${context.locale["commands.command.serverinfo.createdAt"]}", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifference(timeCreated, locale), true)
		embed.addField("\uD83C\uDF1F ${context.locale["commands.command.serverinfo.joinedAt"]}", DateUtils.formatDateWithRelativeFromNowAndAbsoluteDifference(timeJoined, locale), true)
		embed.addField("👥 ${context.locale["commands.command.serverinfo.members"]} ($memberCount)", "", true) // Membros da Guild

		context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
	}
}