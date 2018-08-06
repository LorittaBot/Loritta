package com.mrpowergamerbr.loritta.utils.socket

import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.networkbans.NetworkBanEntry
import com.mrpowergamerbr.loritta.utils.networkbans.NetworkBanType
import kotlinx.coroutines.experimental.launch
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket

class SocketServer(val socketPort: Int) {
	companion object {
		val jsonParser = JsonParser()
		val logger = LoggerFactory.getLogger(SocketServer::class.java)
	}

	fun start() {
		val listener = ServerSocket(socketPort, 0, InetAddress.getLoopbackAddress())
		try {
			while (true) {
				val socket = listener.accept()
				launch {
					try {
						val fromClient = BufferedReader(InputStreamReader(socket.getInputStream(), "UTF-8"))
						val reply = fromClient.readLine()

						logger.trace(reply)

						val jsonObject = jsonParser.parse(reply).obj

						val updateType = UpdateType.valueOf(jsonObject["type"].string)

						when (updateType) {
							UpdateType.BAN_USER -> {
								val guild = lorittaShards.getGuildById(jsonObject["guildId"].string)
								if (guild != null) {
									val serverConfig = loritta.getServerConfigForGuild(jsonObject["guildId"].string)
									val locale = loritta.getLocaleById(serverConfig.localeId)
									BanCommand.ban(
											serverConfig,
											guild,
											guild.selfMember.user,
											locale,
											lorittaShards.retrieveUserById(jsonObject["userId"].string)!!,
											jsonObject["reason"].string,
											false,
											7
									)
								}
							}
							UpdateType.ADD_NETWORK_BAN -> {
								val entry = NetworkBanEntry(jsonObject["userId"].string, NetworkBanType.valueOf(jsonObject["banType"].string), jsonObject["reason"].string)
								loritta.networkBanManager.addBanEntry(entry)
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