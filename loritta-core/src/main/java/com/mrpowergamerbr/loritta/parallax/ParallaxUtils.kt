package com.mrpowergamerbr.loritta.parallax

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.parallax.wrappers.ParallaxEmbed
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.gson
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import org.apache.commons.lang3.exception.ExceptionUtils
import org.graalvm.polyglot.Value
import java.awt.Color
import java.io.File
import java.util.concurrent.ExecutionException

object ParallaxUtils {
	private val logger = KotlinLogging.logger {}

	fun securityViolation(guildId: String?) {
		try {
			throw IllegalArgumentException("Deu ruim!")
		} catch (e: Exception) {
			logger.error(e) { "Descobriram como pegar o meu token usando JavaScript na guild $guildId!!!" }
		}

		File("do_not_start").writeText("")
		System.exit(0)
	}

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

	/**
	 * Sends the [throwable] to a [channel] inside a [net.dv8tion.jda.api.entities.MessageEmbed]
	 *
	 * @return the sent throwable
	 */
	suspend fun sendThrowableToChannel(throwable: Throwable, channel: MessageChannel, message: String? = null): Message {
		logger.warn(throwable) { "Error while evaluating code" }

		val messageBuilder = MessageBuilder()
		messageBuilder.append(message ?: " ")

		val cause = throwable.cause

		val embedBuilder = EmbedBuilder()
		embedBuilder.setTitle("❌ Ih Serjão Sujou! 🤦", "https://youtu.be/G2u8QGY25eU")

		val description = when (throwable) {
			// Thread.stop (deprecated)
			is ExecutionException -> "A thread que executava este comando agora está nos céus... *+angel* (Provavelmente seu script atingiu o limite máximo de memória utilizada!)"
			else -> {
				val stringBuilder = StringBuilder()

				if (cause?.message != null)
					stringBuilder.append("${cause.message}\n")

				stringBuilder.append(ExceptionUtils.getStackTrace(throwable))

				stringBuilder.toString().substringIfNeeded(0 until 2000)
			}
		}

		embedBuilder.setDescription("```$description```")
		embedBuilder.setFooter("Aprender a programar seria bom antes de me forçar a executar códigos que não funcionam 😢", null)
		embedBuilder.setColor(Color.RED)

		messageBuilder.setEmbed(embedBuilder.build())

		return channel.sendMessage(messageBuilder.build()).await()
	}
}