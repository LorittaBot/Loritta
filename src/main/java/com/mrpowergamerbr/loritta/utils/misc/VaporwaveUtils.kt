package com.mrpowergamerbr.loritta.utils.misc

object VaporwaveUtils {
	/**
	 * Converte um texto para full width
	 *
	 * @return O texto em formato full width
	 */
	fun vaporwave(str: String): String {
		var str = str
		str = str // Como a gente abusa dos códigos unicode, é necessário dar lowercase antes de aplicar o efeito
		val sb = StringBuilder()
		for (_c in str.toCharArray()) {
			val isUpperCase = _c.isUpperCase();
			val c = _c.toLowerCase()
			if (Character.isSpaceChar(c)) {
				sb.append(" ")
				continue
			}
			var vaporC = (c.toInt() + 0xFEE0).toChar()

			if (Character.getType(vaporC) != 2) {
				sb.append(c)
				continue
			}

			if (isUpperCase)
				vaporC = vaporC.toUpperCase()
			sb.append(vaporC)
		}
		return sb.toString()
	}
}