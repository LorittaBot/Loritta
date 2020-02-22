package net.perfectdreams.loritta.plugin.funky.audio

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.audio.AudioManager
import com.mrpowergamerbr.loritta.utils.encodeToUrl
import com.mrpowergamerbr.loritta.utils.jsonParser
import io.ktor.client.request.get
import io.ktor.client.request.header
import lavalink.client.io.LavalinkSocket
import net.perfectdreams.loritta.platform.discord.LorittaDiscord

class LavalinkRestClient(val loritta: LorittaDiscord, val audioManager: AudioManager) {
	private fun getRandomLavalinkNode() = audioManager.lavalink.nodes.filter { it.isAvailable }.random()
	private fun getLavalinkUrlForNode(node: LavalinkSocket) = node.remoteUri.toString().replace("ws", "http")
	private fun getPasswordForNode(node: LavalinkSocket) = loritta.discordConfig.lavalink.nodes.first { node.name == it.name }.password

	suspend fun searchTrackOnYouTube(query: String): List<JsonObject> {
		val lavalinkNode = getRandomLavalinkNode()

		val response = loritta.http.get<String>("${getLavalinkUrlForNode(lavalinkNode)}/loadtracks?identifier=ytsearch:${query.encodeToUrl()}") {
			header("Authorization", getPasswordForNode(lavalinkNode))
		}

		val json = jsonParser.parse(response)
		val loadType = json["loadType"].string

		if (loadType != "SEARCH_RESULT")
			throw RuntimeException(loadType)

		return json["tracks"].array.map { it.obj }
	}

	suspend fun loadTrack(audioId: String): String {
		val lavalinkNode = getRandomLavalinkNode()

		val response = loritta.http.get<String>("${getLavalinkUrlForNode(lavalinkNode)}/loadtracks?identifier=${audioId.encodeToUrl()}") {
			header("Authorization", getPasswordForNode(lavalinkNode))
		}

		val json = jsonParser.parse(response)
		val loadType = json["loadType"].string

		if (loadType != "TRACK_LOADED")
			throw RuntimeException(loadType)

		val track = json["tracks"].array.first()["track"].string
		return track
	}
}