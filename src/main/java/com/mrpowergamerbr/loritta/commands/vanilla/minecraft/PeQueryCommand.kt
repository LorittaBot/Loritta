package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.whirvis.jraknet.identifier.MinecraftIdentifier
import com.whirvis.jraknet.util.RakNetUtils
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.net.UnknownHostException
import java.util.*

class PeQueryCommand : AbstractCommand("pequery", category = CommandCategory.MINECRAFT) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["MCQUERY_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "IP do servidor"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("PvP.PigRaid.com")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.size == 1) {
			val ip = context.args[0]
			var hostname = ip
			var port = 19132
			if (ip.contains(":")) {
				// IP + Porta
				hostname = ip.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
				try {
					port = Integer.parseInt(ip.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
				} catch (e: Exception) {
				}
				// Calma cara, já entendi, essa porta tá errada
			}

			try {
				val identifier = RakNetUtils.getServerIdentifier(ip, port)

				if (identifier == null) {
					// Servidor offline
					context.reply(
							locale["MCQUERY_OFFLINE"],
							Constants.ERROR
					)
					return
				}
				val bedrockIdentifier = MinecraftIdentifier(identifier)

				val embed = EmbedBuilder().apply {
					setTitle("<:minecraft_logo:412575161041289217> $hostname:$port", null)
					setColor(Color(87, 132, 74))
					addField("MOTD", bedrockIdentifier.serverName.replace("§[0-9a-fk-or]".toRegex(), ""), false)
					addField(locale["MCQUERY_VERSION"], bedrockIdentifier.versionTag + " (${bedrockIdentifier.serverProtocol})", true)
					addField("Players", "${bedrockIdentifier.onlinePlayerCount}/${bedrockIdentifier.maxPlayerCount}", true)
					if (bedrockIdentifier.gamemode != null) {
						addField("Gamemode", bedrockIdentifier.gamemode, true)
					}
					if (bedrockIdentifier.worldName != null) {
						addField(locale["MCQUERY_WORLD"], bedrockIdentifier.worldName, true)
					}
				}

				context.sendMessage(embed.build())
			} catch (e: UnknownHostException) {
				context.reply(
						locale["MCQUERY_OFFLINE"],
						Constants.ERROR
				)
				// Quando o servidor não existe
			}

			// context.sendMessage(context.getAsMention(true), builder.build())
		} else {
			context.explain()
		}
	}

	private fun addIfExists(builder: EmbedBuilder, serverResponse: JsonObject, name: String, get: String, inline: Boolean) {
		if (serverResponse.has(get)) {
			var value = serverResponse.get(get).asString
			if (get == "motd") {
				value = value.replace("§[0-9a-fk-or]".toRegex(), "")
			}
			if (get == "ping") {
				value = value + "ms"
			}
			builder.addField(name, value, inline)
		}
	}
}
