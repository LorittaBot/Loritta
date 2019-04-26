package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.AllowReflection
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.gson
import net.dv8tion.jda.api.entities.Guild

abstract class ConfigPayloadType(val type: String) {
	abstract fun process(payload: JsonObject, userIdentification: TemmieDiscordAuth.UserIdentification, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, guild: Guild)

	fun applyReflection(payload: JsonObject, type: Any) {
		for ((key, value) in payload.entrySet()) {
			val field = try { type::class.java.getDeclaredField(key) } catch (e: NoSuchFieldException) { continue }

			if (!field.isAnnotationPresent(AllowReflection::class.java))
				continue

			field.isAccessible = true

			when {
				value.isJsonNull -> field.set(type, null)
				field.type.isAssignableFrom(Integer::class.javaObjectType) -> field.set(type, value.int)
				field.type.isAssignableFrom(Double::class.javaObjectType) -> field.set(type, value.double)
				field.type.isAssignableFrom(Float::class.javaObjectType) -> field.set(type, value.float)
				field.type.isAssignableFrom(Boolean::class.javaObjectType) -> field.set(type, value.bool)
				field.type.isAssignableFrom(Long::class.javaObjectType) -> field.set(type, value.long)
				else -> {
					field.set(type, gson.fromJson(value))
				}
			}
		}
	}
}