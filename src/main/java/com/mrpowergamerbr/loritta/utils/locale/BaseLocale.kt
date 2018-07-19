package com.mrpowergamerbr.loritta.utils.locale

import com.google.common.flogger.FluentLogger
import com.mrpowergamerbr.loritta.utils.f

/**
 * Classe de localização base, por padrão em PT-BR
 *
 * Locales diferentes devem extender esta classe
 */
open class BaseLocale {
	companion object {
		private val logger = FluentLogger.forEnclosingClass()
	}

	@Transient
	var strings = mutableMapOf<String, String>()

	operator fun get(key: String, vararg arguments: Any?): String {
		if (!strings.containsKey(key)) {
			logger.atWarning().log("Missing translation key! $key")
			return key
		}
		return strings[key]!!.f(*arguments)
	}

	// Generic
	lateinit var VIEIRINHA_responses: List<String>

	lateinit var SHIP_valor90: List<String>

	lateinit var SHIP_valor80: List<String>

	lateinit var SHIP_valor70: List<String>

	lateinit var SHIP_valor60: List<String>

	lateinit var SHIP_valor50: List<String>

	lateinit var SHIP_valor40: List<String>

	lateinit var SHIP_valor30: List<String>

	lateinit var SHIP_valor20: List<String>

	lateinit var SHIP_valor10: List<String>

	lateinit var SHIP_valor0: List<String>
}