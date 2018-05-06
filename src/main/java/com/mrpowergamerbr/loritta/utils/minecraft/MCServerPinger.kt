package com.mrpowergamerbr.loritta.utils.minecraft

import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.contains
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.jsonParser
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.Charset


object MCServerPinger {
	var PACKET_HANDSHAKE: Int = 0x00
	var PACKET_STATUSREQUEST: Int = 0x00
	var PACKET_PING: Int = 0x01
	var PROTOCOL_VERSION: Int = 4
	var STATUS_HANDSHAKE: Int = 1

	fun ping(address: String, port: Int = 25565, timeout: Int = 5000): MCQueryResponse? {
		val responseLatest = pingLatest(address, port, timeout)

		if (responseLatest != null)
			return responseLatest

		return null
	}

	fun pingLatest(address: String, port: Int = 25565, timeout: Int = 5000): MCQueryResponse? {
		val socket = Socket()
		socket.connect(InetSocketAddress(address, port), timeout)

		val fromServer = DataInputStream(socket.getInputStream())
		val toServer = DataOutputStream(socket.getOutputStream())

		//> Handshake

		val handshake_bytes = ByteArrayOutputStream()
		val handshake = DataOutputStream(handshake_bytes)

		handshake.writeByte(PACKET_HANDSHAKE)
		writeVarInt(handshake, PROTOCOL_VERSION)
		writeVarInt(handshake, address.length)
		handshake.writeBytes(address)
		handshake.writeShort(port)
		writeVarInt(handshake, STATUS_HANDSHAKE)

		writeVarInt(toServer, handshake_bytes.size())
		toServer.write(handshake_bytes.toByteArray())

		//> Status request

		toServer.writeByte(0x01) // Size of packet
		toServer.writeByte(PACKET_STATUSREQUEST)

		//< Status response

		readVarInt(fromServer) // Size
		var id = readVarInt(fromServer)

		if (id == -1) // Server prematurely ended stream
			return null

		if (id != PACKET_STATUSREQUEST) // Server returned invalid packet
			return null

		val length = readVarInt(fromServer)

		if (length == -1) // Server prematurely ended stream
			return null

		if (length == 0) // Server returned unexpected value
			return null

		val data = ByteArray(length)
		fromServer.readFully(data)
		val _json = String(data, Charset.forName("UTF-8"))

		//> Ping

		toServer.writeByte(0x09) // Size of packet
		toServer.writeByte(PACKET_PING)
		toServer.writeLong(System.currentTimeMillis())

		//< Ping

		readVarInt(fromServer) // Size
		id = readVarInt(fromServer)

		if (id == -1) // Server prematurely ended stream
			return null

		if (id != PACKET_PING) // Server returned unknown packet
			return null

		// Close

		handshake.close()
		handshake_bytes.close()
		toServer.close()
		fromServer.close()
		socket.close()

		val json = jsonParser.parse(_json).obj

		val versionName = json["version"]["name"].string
		val versionProtocol = json["version"]["protocol"].int
		val playersOnline = json["players"]["online"].int
		val playersMax = json["players"]["max"].int
		val favicon = json["favicon"].nullString

		val isPre1_10Motd = json["description"].isJsonPrimitive
		val builder = StringBuilder()

		if (isPre1_10Motd) {
			builder.append(json["description"].string.replace("§[0-9a-fk-or]".toRegex(), ""))
		} else {
			val descriptionRootComponent = json["description"].obj

			fun parseComponent(builder: StringBuilder, obj: JsonObject): StringBuilder {
				if (obj.contains("extra")) {
					obj["extra"].array.forEach {
						parseComponent(builder, it.obj)
					}
				}
				return builder.append(obj["text"].string)
			}

			parseComponent(builder, descriptionRootComponent)
		}

		return MCQueryResponse(builder.toString(), versionName, versionProtocol, playersOnline, playersMax, favicon)
	}

	class MCQueryResponse(val motd: String, val versionName: String, val versionProtocol: Int, val playersOnline: Int, val playersMax: Int, val favicon: String?)

	/**
	 * @author thinkofdeath
	 * See: https://gist.github.com/thinkofdeath/e975ddee04e9c87faf22
	 */
	@Throws(IOException::class)
	fun readVarInt(`in`: DataInputStream): Int {
		var i = 0
		var j = 0
		while (true) {
			val k = `in`.readByte().toInt()

			i = i or (k and 0x7F shl j++ * 7)

			if (j > 5)
				throw RuntimeException("VarInt too big")

			if (k and 0x80 != 128)
				break
		}

		return i
	}

	/**
	 * @author thinkofdeath
	 * See: https://gist.github.com/thinkofdeath/e975ddee04e9c87faf22
	 * @throws IOException
	 */
	@Throws(IOException::class)
	fun writeVarInt(out: DataOutputStream, paramInt: Int) {
		var paramInt = paramInt
		while (true) {
			if (paramInt and -0x80 == 0) {
				out.writeByte(paramInt)
				return
			}

			out.writeByte(paramInt and 0x7F or 0x80)
			paramInt = paramInt ushr 7
		}
	}
}