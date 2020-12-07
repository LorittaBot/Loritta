package net.perfectdreams.loritta.commands.vanilla.discord

import com.github.salomonbrys.kotson.nullString
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.Emotes

class ServerIconCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("servericon", "guildicon"), CommandCategory.DISCORD) {
	companion object {
		private const val LOCALE_PREFIX = "commands.discord.servericon"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.description")

		canUseInPrivateChannel = false

		executesDiscord {
			val context = this

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

			val name = guild["name"].nullString
			val iconUrl = guild["iconUrl"].nullString

			if (iconUrl == null) {
				context.reply(
						LorittaReply(
								message = context.locale["$LOCALE_PREFIX.noIcon", Emotes.LORI_PAT],
								prefix = Constants.ERROR
						)
				)
				return@executesDiscord
			}

			val embed = EmbedBuilder()
			embed.setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)
			val description = "**${context.locale["commands.discord.avatar.clickHere", "$iconUrl?size=2048"]}**"

			embed.setDescription(description)
			embed.setImage(iconUrl) // Ícone da Guild
			embed.setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)
			embed.setTitle("<:discord:314003252830011395> $name", null) // Nome da Guild
			embed.setImage("${iconUrl.replace("jpg", "png")}?size=2048")

			context.sendMessage(context.getUserMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
		}
	}
}