package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import java.awt.Color
import java.util.*

class McQueryCommand : CommandBase() {
	override fun getLabel(): String {
		return "mcquery"
	}

	override fun getDescription(): String {
		return "Mostra quantos players um servidor de Minecraft tem"
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MINECRAFT
	}

	override fun getUsage(): String {
		return "IP do servidor"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("jogar.sparklypower.net")
	}

	override fun run(context: CommandContext) {
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

			var body = HttpRequest.get("https://mcapi.ca/query/$hostname:$port/extensive").body()
			// Vamos tentar realizar primeiro uma extensive query
			var serverResponse = JsonParser().parse(body).asJsonObject // Base
			// Nós também iremos pegar a "plain response", para alguns servidores aonde a extensive query é muito... "extensive" (exemplo: MOTD)
			body = HttpRequest.get("https://mcapi.ca/query/$hostname:$port/info").body()
			// Vamos tentar realizar primeiro uma extensive query
			val plainResponse = JsonParser().parse(body).asJsonObject // Response padrão então

			if (serverResponse.has("error")) {
				serverResponse = plainResponse // Ok, se serverResponse (extensive) está com erro, vamos apenas "trocar" a resposta
			}
			if (serverResponse.has("error")) { // E se ainda está com erro... bem, desisto.
				// desisto :(
				context.sendMessage(LorittaUtils.ERROR + " | " + context.getAsMention(true) + "Servidor `" + ip + ":" + port + "` não existe ou está offline!")
				return
			}
			val builder = EmbedBuilder()

			builder.setColor(Color.GREEN)

			builder.setTitle("<:grass:330435576392318978> $hostname:$port", null)

			addIfExists(builder, plainResponse, "MOTD", "motd", false) // Vamos usar o plain, o extensive pega muitas coisas do BungeeCord
			addIfExists(builder, plainResponse, "Ping", "ping", true) // Ping apenas no plain

			if (plainResponse!!.has("players")) { // Plain novamente, já que o extensive mostra o player count do BungeeCord... normalmente é 1
				builder.addField("Players", plainResponse.getAsJsonObject("players").get("online").asString + "/" + plainResponse.getAsJsonObject("players").get("max").asString, true)
			}

			addIfExists(builder, serverResponse, "Software", "software", true)
			addIfExists(builder, serverResponse, "Versão", "version", true)
			addIfExists(builder, plainResponse, "Protocolo", "protocol", true) // Protocolo só tem no plain

			if (serverResponse.has("list")) { // Players online
				val list = StringBuilder()

				var first = true
				for (str in serverResponse.get("list").asJsonArray) {
					if (first) {
						list.append(str.asString.replace("_", "\\_")) // Um pouco de "strip markdown"
						first = false
					} else {
						list.append(", " + str.asString.replace("_", "\\_"))
					}
				}
				builder.addField("Players", list.toString(), true)
			}

			if (serverResponse.has("plugins")) { // Players online
				val plugins = serverResponse.get("plugins").asString
				builder.addField("Plugins", if (plugins.isEmpty()) "¯\\_(ツ)_/¯" else plugins.toString(), false)
			}

			builder.setThumbnail("https://mcapi.ca/query/$hostname:$port/icon") // E agora o server-icon do servidor

			val message = MessageBuilder().append(context.getAsMention(true)).setEmbed(builder.build()).build()

			context.sendMessage(message)
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