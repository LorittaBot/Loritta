package com.mrpowergamerbr.loritta.parallax

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import org.apache.commons.lang3.exception.ExceptionUtils
import java.awt.Color
import java.util.concurrent.ExecutionException

object ParallaxUtils {
	private val logger = KotlinLogging.logger {}

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

	fun transformToJson(message: Message): JsonObject {
		return jsonObject(
				"id" to message.idLong,
				"author" to transformToJson(message.author),
				"textChannelId" to message.channel.idLong,
				"content" to message.contentRaw,
				"cleanContent" to message.contentStripped,
				"mentionedUsers" to message.mentionedUsers.map { transformToJson(it) }.toJsonArray()
		)
	}

	fun transformToJson(user: User): JsonObject {
		return jsonObject(
				"id" to user.idLong,
				"username" to user.name,
				"discriminator" to user.discriminator,
				"avatar" to user.avatarId
		)
	}

	fun transformToJson(member: Member): JsonObject {
		return jsonObject(
				"user" to transformToJson(member.user),
				"nickname" to member.effectiveName,
				"roleIds" to member.roles.map { it.idLong }.toJsonArray(),
				"joinedAt" to member.timeJoined.toInstant().toEpochMilli(),
				"premiumSince" to member.timeBoosted?.toInstant()?.toEpochMilli()
		)
	}
}