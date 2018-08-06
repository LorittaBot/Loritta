package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.userdata.AllowReflection
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.gson
import net.dv8tion.jda.core.entities.Guild
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

abstract class ConfigPayloadType(val type: String) {
	abstract fun process(payload: JsonObject, serverConfig: ServerConfig, guild: Guild)

	fun applyReflection(payload: JsonObject, type: Any) {
		for ((key, value) in payload.entrySet()) {
			val field = type::class.memberProperties.firstOrNull { it.name == key } ?: continue

			if (!field.annotations.any { it.javaClass == AllowReflection::class.java})
				continue

			val mutable = field as KMutableProperty<*>
			mutable.isAccessible = true

			when {
				value.isJsonNull -> mutable.setter.call(null)
				mutable.returnType.isSubtypeOf(Integer::class.createType(nullable = true)) -> mutable.setter.call(value.int)
				mutable.returnType.isSubtypeOf(Double::class.createType(nullable = true)) -> mutable.setter.call(value.double)
				mutable.returnType.isSubtypeOf(Float::class.createType(nullable = true)) -> mutable.setter.call(value.float)
				mutable.returnType.isSubtypeOf(Boolean::class.createType(nullable = true)) -> mutable.setter.call(value.bool)
				mutable.returnType.isSubtypeOf(Long::class.createType(nullable = true)) -> mutable.setter.call(value.long)
				else -> {
					mutable.setter.call(gson.fromJson(value))
				}
			}
		}
	}
}