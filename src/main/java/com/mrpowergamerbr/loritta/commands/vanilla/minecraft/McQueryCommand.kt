package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.minecraft.MCServerPinger
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.util.*

class McQueryCommand : AbstractCommand("mcquery", category = CommandCategory.MINECRAFT) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["MCQUERY_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "IP do servidor"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("mc.hypixel.net")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.size == 1) {
			val ip = context.args[0]
			var hostname = ip
			var port = 25565
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
				val response = MCServerPinger.ping(hostname, port)

				if (response == null) {
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["MCQUERY_OFFLINE", ip, port])
					return
				}

				val builder = EmbedBuilder()

				builder.setColor(Color.GREEN)

				builder.setTitle("<:minecraft_logo:412575161041289217> $hostname:$port", null)

				builder.addField("MOTD", response.motd, false)
				builder.addField("Players", response.playersOnline.toString() + "/" + response.playersMax, true)
				builder.addField(locale["MCQUERY_VERSION"], response.versionName + " (${response.versionProtocol})", true)

				builder.setThumbnail("https://mcapi.ca/query/$hostname:$port/icon") // E agora o server-icon do servidor

				context.sendMessage(context.getAsMention(true), builder.build())
			} catch (e: Exception) {
				e.printStackTrace()
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["MCQUERY_OFFLINE", ip, port])
			}
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
