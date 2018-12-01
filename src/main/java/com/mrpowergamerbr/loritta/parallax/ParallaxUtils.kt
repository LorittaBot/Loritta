package com.mrpowergamerbr.loritta.parallax

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxEmbed
import com.mrpowergamerbr.loritta.utils.gson
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Message
import org.graalvm.polyglot.Value

object ParallaxUtils {
	fun toParallaxMessage(map: Map<*, *>): Message {
		val jsonObject = convertPolyglotMapToJson(map).obj
		val builder = MessageBuilder()
		if (jsonObject.has("content"))
			builder.setContent(jsonObject["content"].string)

		if (jsonObject.has("embed")) {
			val embed = jsonObject["embed"]
			val fields = embed["fields"].nullObj

			if (fields != null) {
				val fixedFields = JsonArray()

				for ((_, value) in fields.entrySet()) {
					fixedFields.add(value)
				}

				embed["fields"] = fixedFields
			}

			builder.setEmbed(gson.fromJson<ParallaxEmbed>(embed).toDiscordEmbed())
		}

		return builder.build()
	}

	fun toParallaxEmbed(value: Value): ParallaxEmbed {
		return gson.fromJson(convertValueToJson(value))
	}

	fun convertPolyglotMapToJson(map: Map<*, *>): JsonElement {
		return gson.toJsonTree(map)
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