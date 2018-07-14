package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.userdata.AllowReflection
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.gson
import net.dv8tion.jda.core.entities.Guild

abstract class ConfigPayloadType(val type: String) {
	abstract fun process(payload: JsonObject, serverConfig: ServerConfig, guild: Guild)

	fun applyReflection(payload: JsonObject, type: Any) {
		for ((key, value) in payload.entrySet()) {
			val field = try { type::class.java.getDeclaredField(key) } catch (e: NoSuchFieldException) { continue }

			if (!field.isAnnotationPresent(AllowReflection::class.java))
				continue

			field.isAccessible = true

			when {
				value.isJsonNull -> field.set(type, null)
				field.type.isAssignableFrom(Integer::class.java) -> field.setInt(type, value.int)
				field.type.isAssignableFrom(Double::class.java) -> field.setDouble(type, value.double)
				field.type.isAssignableFrom(Float::class.java) -> field.setFloat(type, value.float)
				field.type.isAssignableFrom(Boolean::class.java) -> field.setBoolean(type, value.bool)
				else -> {
					field.set(type, gson.fromJson(value))
				}
			}
		}
	}
}