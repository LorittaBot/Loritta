package com.mrpowergamerbr.loritta.commands.nashorn

import com.github.salomonbrys.kotson.addAll
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import com.mrpowergamerbr.loritta.utils.MessageUtils
import net.perfectdreams.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import io.ktor.client.request.*
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.serializable.CustomCommandCodeType
import net.perfectdreams.loritta.utils.ExperienceUtils
import net.perfectdreams.loritta.utils.NetAddressUtils

/**
 * Comandos usando a Nashorn Engine
 */
class NashornCommand(label: String, val javaScriptCode: String, val codeType: CustomCommandCodeType) : AbstractCommand(label, category = CommandCategory.MISC) {
	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		when (codeType) {
			CustomCommandCodeType.KOTLIN -> {
				val members = JsonArray()

				members.add(ParallaxUtils.transformToJson(context.guild.selfMember))
				members.add(ParallaxUtils.transformToJson(context.message.member!!))
				members.addAll(context.message.mentionedMembers.map { ParallaxUtils.transformToJson(it) })

				val roles = JsonArray()

				context.guild.roles.forEach {
					roles.add(
							jsonObject(
									"id" to it.idLong,
									"name" to it.name
							)
					)
				}

				val commandRequest = jsonObject(
						"code" to javaScriptCode,
						"label" to label,
						"lorittaClusterId" to loritta.lorittaCluster.id,
						"message" to ParallaxUtils.transformToJson(context.message),
						"guild" to jsonObject(
								"id" to context.guild.idLong,
								"name" to context.guild.name,
								"members" to members,
								"roles" to roles
						),
						"args" to context.rawArgs.toList().toJsonArray(),
						"clusterUrl" to "https://${loritta.lorittaCluster.getUrl()}"
				)

				val result = loritta.http.post<String>("http://" + NetAddressUtils.fixIp(loritta.config.parallaxCodeServer.url) + "/api/v1/parallax/process-command") {
					this.body = commandRequest.toString()
				}

				println(result)
			}
			CustomCommandCodeType.SIMPLE_TEXT -> {
				val customTokens = mutableMapOf<String, String>()

				if (javaScriptCode.contains("{experience") || javaScriptCode.contains("{level") || javaScriptCode.contains("{xp")) {
					customTokens.putAll(
							ExperienceUtils.getExperienceCustomTokens(
									context.config,
									context.handle
							)
					)
				}

				val message = MessageUtils.generateMessage(
						javaScriptCode,
						listOf(
								context.handle,
								context.guild,
								context.message.channel
						),
						context.guild,
						customTokens = customTokens
				) ?: return

				context.sendMessage(message)
			}
			else -> throw RuntimeException("Unsupported code type $codeType")
		}
	}
}