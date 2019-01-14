package com.mrpowergamerbr.loritta.commands.nashorn

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

	@Throws(IOException::class)
	@JvmStatic
	fun createColor(r: Int, g: Int, b: Int): Color {
		return Color(r, g, b)
	}
}