package com.mrpowergamerbr.loritta.parallax

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxEmbed
import jdk.nashorn.api.scripting.ScriptObjectMirror

object ParallaxUtils {
	fun toParallaxEmbed(obj: ScriptObjectMirror): ParallaxEmbed {
		val json = Loritta.GSON.toJsonTree(obj)["embed"].obj

		val fields = json["fields"].nullObj

		if (fields != null) {
			val fixedFields = JsonArray()

			for ((_, value) in fields.entrySet()) {
				fixedFields.add(value)
			}

			json["fields"] = fixedFields
		}
		return Loritta.GSON.fromJson(json)
	}
}