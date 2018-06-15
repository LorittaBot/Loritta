package com.mrpowergamerbr.loritta.utils

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.website.LoriWebCode
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object WebsiteUtils {
	/**
	 * Creates an JSON object wrapping the error object
	 *
	 * @param code    the error code
	 * @param message the error reason
	 * @return        the json object containing the error
	 */
	fun createErrorPayload(code: LoriWebCode, message: String? = null): JsonObject {
		return jsonObject("error" to createErrorObject(code, message))
	}

	/**
	 * Creates an JSON object containing the code error
	 *
	 * @param code    the error code
	 * @param message the error reason
	 * @return        the json object with the error
	 */
	fun createErrorObject(code: LoriWebCode, message: String? = null): JsonObject {
		val jsonObject = jsonObject(
				"code" to code.errorId,
				"reason" to code.fancyName,
				"help" to "https://loritta.website/docs/api"
		)

		if (message != null) {
			jsonObject["message"] = message
		}

		return jsonObject
	}

	/**
	 * Builds a URL queries using the parameters provided in the map
	 *
	 * @param params the map containing all variables to be used in the query
	 * @return       the query string
	 */
	fun buildQuery(params: Map<String, Any>): String {
		val query = arrayOfNulls<String>(params.size)
		for ((index, key) in params.keys.withIndex()) {
			var value = (if (params[key] != null) params[key] else "").toString()
			try {
				value = URLEncoder.encode(value, "UTF-8")
			} catch (e: UnsupportedEncodingException) {
			}

			query[index] = "$key=$value"
		}

		return query.joinToString("&")
	}
}