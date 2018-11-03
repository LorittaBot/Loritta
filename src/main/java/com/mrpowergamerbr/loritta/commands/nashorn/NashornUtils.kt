package com.mrpowergamerbr.loritta.commands.nashorn

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.nashorn.wrappers.NashornImage
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import java.awt.Color
import java.io.IOException

/**
 * Classe de utilidades para comandos usando o Nashorn
 */
object NashornUtils {
	@JvmStatic
	fun loritta(): String { // Método teste
		return "Loritta!"
	}
	@JvmStatic
	fun getURL(url: String): String {
		return HttpRequest.get(url).userAgent(Constants.USER_AGENT).body()
	}

	@Throws(IOException::class)
	@JvmStatic
	fun downloadImage(url: String): NashornImage {
		val image = LorittaUtils.downloadImage(url)
		return NashornImage(image!!)
	}

	@Throws(IOException::class)
	@JvmStatic
	fun createColor(r: Int, g: Int, b: Int): Color {
		return Color(r, g, b)
	}
}