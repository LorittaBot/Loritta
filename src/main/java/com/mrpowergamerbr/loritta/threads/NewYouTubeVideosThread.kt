package com.mrpowergamerbr.loritta.threads

import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta.Companion.GSON
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import kotlinx.coroutines.experimental.*
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class NewYouTubeVideosThread : Thread("YouTube Query Thread") {
	companion object {
		val logger = LoggerFactory.getLogger(NewYouTubeVideosThread::class.java)
	}

	class YouTubeVideo(val id: String)

	override fun run() {
		super.run()

		while (true) {
			try {
				checkNewVideos()
			} catch (e: Exception) {
				logger.error("Erro ao verificar novos vídeos!", e)
			}
			Thread.sleep(15000)
		}
	}

	fun getResponseError(json: JsonObject): String? {
		if (!json.has("error"))
			return null

		return json["error"]["errors"][0]["reason"].string
	}

	fun checkNewVideos() {
		// Servidores que usam o módulo do YouTube
		val servers = loritta.serversColl.find(
				Filters.gt("youTubeConfig.channels", listOf<Any>())
		)

		// IDs dos canais a serem verificados
		var channelIds = mutableSetOf<String>()

		val list = mutableListOf<ServerConfig>()

		logger.info("Verificando canais do YouTube de ${servers.count()} servidores...")

		servers.iterator().use {
			while (it.hasNext()) {
				val server = it.next()
				val guild = lorittaShards.getGuildById(server.guildId) ?: continue
				val youTubeConfig = server.youTubeConfig

				for (channel in youTubeConfig.channels) {
					if (channel.channelId == null)
						continue
					if (!channel.channelUrl!!.startsWith("http"))
						continue
					val textChannel = guild.getTextChannelById(channel.repostToChannelId) ?: continue

					if (!textChannel.canTalk())
						continue

					channelIds.add(channel.channelId!!)
				}
				list.add(server)
			}
		}

		File("./youtube_channels.txt").writeText(channelIds.joinToString("\n"))

		logger.info("Existem ${channelIds.size} canais no YouTube que eu irei verificar!")

		fun sendToRelayShard(list: List<String>, port: Int) {
			val obj = JsonObject()
			obj["channelIds"] = GSON.toJsonTree(list)
			val s = Socket("127.0.0.1", port)
			val toServer = OutputStreamWriter(s.getOutputStream(), "UTF-8")
			val fromServer = BufferedReader(InputStreamReader(s.getInputStream(), "UTF-8"))

			toServer.write(obj.toString() + "\n")
			toServer.flush()

			val response = fromServer.readLine()
			s.close()
			fromServer.close()
		}

		val shard0 = channelIds.filter { Math.abs(it.hashCode()) % 4 == 0 }
		val shard1 = channelIds.filter { Math.abs(it.hashCode()) % 4 == 1 }
		val shard2 = channelIds.filter { Math.abs(it.hashCode()) % 4 == 2 }
		val shard3 = channelIds.filter { Math.abs(it.hashCode()) % 4 == 3 }

		// enviar para todas as shards
		sendToRelayShard(shard0, 10700)
		sendToRelayShard(shard1, 10701)
		sendToRelayShard(shard2, 10702)
		sendToRelayShard(shard3, 10703)
	}
}