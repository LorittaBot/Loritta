package net.perfectdreams.loritta.plugin.funky.commands

import com.mrpowergamerbr.loritta.utils.LorittaPermission
import lavalink.client.io.LavalinkSocket
import lavalink.client.io.Link
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.funky.FunkyPlugin
import net.perfectdreams.loritta.plugin.funky.commands.base.DSLCommandBase
import org.json.JSONArray
import org.json.JSONObject

object VolumeCommand : DSLCommandBase {
	fun sendFilters(link: Link, vararg jsonObject: JSONObject) {
		// Send WS message
		val node: LavalinkSocket = link.getNode(false) ?: return

		val json = JSONObject()
		json.put("op", "filter")
		json.put("guildId", link.guildId)

		val array = JSONArray()
		for (obj in jsonObject) {
			array.put(obj)
		}
		json.put("filters", array)

		node.send(json.toString())
	}

	override fun command(loritta: LorittaBot, m: FunkyPlugin) = create(loritta, listOf("volume")) {
		description { it["commands.audio.volume.description"] }

		userRequiredLorittaPermissions = listOf(LorittaPermission.DJ)

		executesDiscord {
			val audioManager = m.funkyManager

			val musicManager = audioManager.getMusicManager(guild) ?: return@executesDiscord

			val link = musicManager.link
			if (args[0] == "nightcore") {
				sendFilters(
						link,
						JSONObject()
								.put("type", "timescale")
								.put("speed", 1.25)
								.put("pitch", 1.25)
				)

				// reply("Alterado, yay!")
				return@executesDiscord
			} else if (args[0] == "vaporwave") {
				sendFilters(
						link,
						JSONObject()
								.put("type", "timescale")
								.put("speed", 0.7)
								.put("pitch", 0.8)
				)

				// reply("Alterado, yay!")
				return@executesDiscord
			} else if (args[0] == "reset") {
				sendFilters(
						link
				)
				return@executesDiscord
			}

			// Limpar lista de qualquer música que tenha
			musicManager.scheduler.player.volume = args[0].toInt()

			musicManager.scheduler.player
			reply(
					LorittaReply(
							"Volume alterado!"
					)
			)
		}
	}
}