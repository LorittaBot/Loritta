package com.mrpowergamerbr.loritta.parallax.wrappers

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.Loritta
import jdk.nashorn.api.scripting.ScriptObjectMirror

object ParallaxUtils {
	fun toParallaxEmbed(obj: ScriptObjectMirror): ParallaxEmbed {
		val json = Loritta.GSON.toJsonTree(obj)["embed"]

		val fields = json["fields"].obj
		val fixedFields = JsonArray()

		for ((_, value) in fields.entrySet()) {
			fixedFields.add(value)
		}

		json["fields"] = fixedFields
		return Loritta.GSON.fromJson(json)
	}
}