package com.mrpowergamerbr.loritta.utils.misc

object VaporwaveUtils {
	/**
	 * Converte um texto para full width
	 *
	 * @return O texto em formato full width
	 */
	fun vaporwave(str: String): String {
		var str = str
		str = str.toLowerCase() // Como a gente abusa dos códigos unicode, é necessário dar lowercase antes de aplicar o efeito
		val sb = StringBuilder()
		for (c in str.toCharArray()) {
			if (Character.isSpaceChar(c)) {
				sb.append(" ")
				continue
			}
			val vaporC = (c.toInt() + 0xFEE0).toChar()

			if (Character.getType(vaporC) != 2) {
				sb.append(c)
				continue
			}

			sb.append(vaporC)
		}
		return sb.toString()
	}
}