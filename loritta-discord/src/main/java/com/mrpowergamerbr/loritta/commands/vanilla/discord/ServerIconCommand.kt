package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.github.salomonbrys.kotson.nullString
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.isValidSnowflake
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.utils.Emotes

class ServerIconCommand : AbstractCommand("servericon", listOf("guildicon", "iconeserver", "iconeguild", "iconedoserver", "iconedaguild", "íconedoserver", "iconedoservidor", "íconeguild", "íconedoserver", "íconedaguild", "íconedoservidor"), category = CommandCategory.DISCORD) {
	companion object {
		private const val LOCALE_PREFIX = "commands.command.servericon"
	}

	override fun getDescriptionKey() = LocaleKeyData("$LOCALE_PREFIX.description")
	override fun getExamplesKey() = LocaleKeyData("$LOCALE_PREFIX.examples")

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		var guild: JsonObject? = null

		var guildId = context.guild.idLong

		if (context.rawArgs.isNotEmpty()) {
			val id = context.rawArgs.first()
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
                            Emotes.LORI_HM
                    )
			)
			return
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
			return
		}

		val embed = EmbedBuilder()
		embed.setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)
		val description = "**${context.locale["${AvatarCommand.LOCALE_PREFIX}.clickHere", "$iconUrl?size=2048"]}**"

		val guildIconUrl = iconUrl

		embed.setDescription(description)
		embed.setImage(iconUrl) // Ícone da Guild
		embed.setColor(Constants.DISCORD_BLURPLE) // Cor do embed (Cor padrão do Discord)
		embed.setTitle("<:discord:314003252830011395> ${name}", null) // Nome da Guild
		embed.setImage("${guildIconUrl.replace("jpg", "png")}?size=2048")

		context.sendMessage(context.getAsMention(true), embed.build()) // phew, agora finalmente poderemos enviar o embed!
	}
}