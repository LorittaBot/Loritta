package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mongodb.client.model.Filters
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket

class SocketServer(val socketPort: Int) {
	companion object {
		val jsonParser = JsonParser()
		val logger = LoggerFactory.getLogger(SocketServer::class.java)
	}

	fun start() {
		val listener = ServerSocket(socketPort)
		try {
			while (true) {
				val socket = listener.accept()
				launch {
					try {
						val fromClient = BufferedReader(InputStreamReader(socket.getInputStream(), "UTF-8"))
						val reply = fromClient.readLine()

						logger.trace(reply)

						val jsonObject = jsonParser.parse(reply).obj

						val videoId = jsonObject["videoId"].string
						val title = jsonObject["title"].string
						val channelId = jsonObject["channelId"].string

						val servers = loritta.serversColl.find(
								Filters.eq("youTubeConfig.channels.channelId", channelId)
						).iterator()

						logger.info("Recebi notificação de vídeo $title ($videoId) de $channelId")

						servers.use {
							while (it.hasNext()) {
								val config = it.next()

								val guild = lorittaShards.getGuildById(config.guildId) ?: continue

								val youTubeInfos = config.youTubeConfig.channels.filter { it.channelId == channelId }

								for (youTubeInfo in youTubeInfos) {
									val textChannel = guild.getTextChannelById(youTubeInfo.repostToChannelId) ?: continue

									if (!textChannel.canTalk())
										continue

									var message = youTubeInfo.videoSentMessage ?: "{link}";

									if (message.isEmpty()) {
										message = "{link}"
									}

									val customTokens = mapOf(
											"título" to title,
											"title" to title,
											"link" to "https://youtu.be/" + videoId,
											"video-id" to videoId
									)

									val discordMessage = MessageUtils.generateMessage(
											message,
											listOf(guild),
											guild,
											customTokens
									) ?: continue

									textChannel.sendMessage(discordMessage).queue()
								}
							}
						}

						val response = JsonObject()
						response["type"] = "noop"

						val out = PrintWriter(socket.getOutputStream(), true)
						out.println(response.toString() + "\n")
						out.flush()
						fromClient.close()
					} finally {
						socket.close()
					}
				}
			}
		} finally {
			listener.close()
		}
	}
}