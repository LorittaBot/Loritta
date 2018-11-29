package com.mrpowergamerbr.loritta.parallax

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxEmbed
import com.mrpowergamerbr.loritta.utils.gson
import org.graalvm.polyglot.Value

object ParallaxUtils {
	fun toParallaxEmbed(value: Value): ParallaxEmbed {
		return gson.fromJson(convertValueToJson(value))
	}

	fun convertValueToJson(value: Value): JsonElement {
		val json = JsonObject()
		value.memberKeys.forEach {
			val member = value.getMember(it)
			// TODO: Será que não existe um jeito melhor de detectar se um objeto é um "Object"?
			when {
				member.metaObject.toString() == "Object" -> json[it] = convertValueToJson(member)
				member.hasArrayElements() -> {
					val list = mutableListOf<Any?>()
					for (idx in 0 until member.arraySize) {
						val element = member.getArrayElement(idx)
						list.add(
								convertValueToJson(element)
						)
					}
					json[it] = gson.toJsonTree(list)
				}
				else -> json[it] = convertValueToType(member)
			}
		}

		return json
	}

	fun convertValueToType(member: Value): Any? {
		return when {
			member.isBoolean -> member.asBoolean()
			member.isNumber && member.fitsInLong() -> member.asLong()
			member.isNumber && member.fitsInFloat() -> member.asFloat()
			member.isNumber && member.fitsInDouble() -> member.asDouble()
			member.isNumber && member.fitsInInt() -> member.asInt()
			member.isNumber && member.fitsInShort() -> member.asShort()
			member.isNumber && member.fitsInByte() -> member.asByte()
			member.isString -> member.asString()
			member.hasArrayElements() -> {
				val list = mutableListOf<Any?>()
				for (idx in 0 until member.arraySize) {
					val element = member.getArrayElement(idx)
					list.add(
							convertValueToType(element)
					)
				}
				gson.toJsonTree(list)
			}
			member.isNull -> null
			else -> null
		}
	}
}