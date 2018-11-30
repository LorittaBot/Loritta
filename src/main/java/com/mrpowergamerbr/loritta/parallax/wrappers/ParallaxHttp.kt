package com.mrpowergamerbr.loritta.parallax.wrappers

import com.mrpowergamerbr.loritta.utils.Constants
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO

object ParallaxHttp {
	@JvmStatic
	fun get(url: String): ParallaxHttpResponse {
		val imageUrl = URL(url)
		val connection = imageUrl.openConnection() as HttpURLConnection
		connection.setRequestProperty("User-Agent", Constants.USER_AGENT)

		connection.readTimeout = 10000
		connection.connectTimeout = 10000

		return ParallaxHttpResponse(connection)
	}

	class ParallaxHttpResponse(private val connection: HttpURLConnection) {
		fun asImage(): ParallaxImage {
			return ParallaxImage(ImageIO.read(connection.inputStream))
		}

		fun asString(): String {
			return toString()
		}

		override fun toString(): String {
			return connection.inputStream.bufferedReader().readText()
		}
	}
}