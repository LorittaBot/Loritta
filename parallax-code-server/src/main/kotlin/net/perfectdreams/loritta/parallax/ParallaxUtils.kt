package net.perfectdreams.loritta.parallax

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.perfectdreams.loritta.parallax.ParallaxServer.Companion.gson
import net.perfectdreams.loritta.parallax.wrapper.ParallaxEmbed
import org.graalvm.polyglot.Value
import org.graalvm.polyglot.proxy.ProxyArray
import org.graalvm.polyglot.proxy.ProxyObject

object ParallaxUtils {
	fun toParallaxMessage(map: Map<*, *>): MessageWrapper {
		val jsonObject = convertPolyglotMapToJson(map).obj

		var content: String? = null
		var parallaxEmbed: ParallaxEmbed? = null

		if (jsonObject.has("content"))
			content = jsonObject["content"].string

		if (jsonObject.has("embed")) {
			val embed = jsonObject["embed"].obj
			val fields = embed["fields"].nullObj

			if (fields != null) {
				println("$fields")
				val fixedFields = JsonArray()

				for ((_, value) in fields.entrySet()) {
					fixedFields.add(value)
				}

				embed["fields"] = fixedFields
			}

			parallaxEmbed = gson.fromJson(embed)
		}

		return MessageWrapper(content, parallaxEmbed)
	}

	fun toParallaxEmbed(value: Value): ParallaxEmbed {
		return gson.fromJson(convertValueToJson(value))
	}

	fun convertPolyglotMapToJson(map: Map<*, *>) = gson.toJsonTree(map)

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
			member.metaObject.toString() == "Object" -> convertValueToJson(member)
			else -> null
		}
	}

	class MessageWrapper(
			val content: String?,
			val embed: ParallaxEmbed?
	)

	class ParallaxDataStoreProxy(val values: MutableMap<String, Any?>) : ProxyObject {
		override fun putMember(key: String, value: Value) {
			if (value.isNull)
				values.remove(key)
			else
				values[key] = if (value.isHostObject) value.asHostObject<Any>() else value
		}

		override fun hasMember(key: String?): Boolean {
			return values.containsKey(key)
		}

		override fun getMemberKeys(): Any? {
			return object : ProxyArray {
				private val keys: Array<Any> = values.keys.toTypedArray()
				override fun set(index: Long, value: Value) {
					throw UnsupportedOperationException()
				}

				override fun getSize(): Long {
					return keys.size.toLong()
				}

				override fun get(index: Long): Any {
					if (index < 0 || index > Int.MAX_VALUE) {
						throw ArrayIndexOutOfBoundsException()
					}
					return keys[index.toInt()]
				}
			}
		}

		override fun getMember(key: String?): Any? {
			return values[key]
		}

		override fun removeMember(key: String?): Boolean {
			return if (values.containsKey(key)) {
				values.remove(key)
				true
			} else {
				false
			}
		}
	}
}