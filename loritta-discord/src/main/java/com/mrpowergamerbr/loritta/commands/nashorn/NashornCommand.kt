package com.mrpowergamerbr.loritta.commands.nashorn

import com.github.salomonbrys.kotson.addAll
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.parallax.ParallaxUtils
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import io.ktor.client.request.post
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.serializable.CustomCommandCodeType
import net.perfectdreams.loritta.utils.ExperienceUtils
import net.perfectdreams.loritta.utils.NetAddressUtils
import net.perfectdreams.loritta.utils.Placeholders
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

/**
 * Comandos usando a Nashorn Engine
 */
class NashornCommand(label: String, val javaScriptCode: String, val codeType: CustomCommandCodeType) : AbstractCommand(label, category = CommandCategory.MISC) {
	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
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
					// Load tokens for experience/level/xp
					val profile = context.config.getUserData(context.userHandle.idLong)
					val level = profile.getCurrentLevel().currentLevel
					val xp = profile.xp

					val currentLevelTotalXp = ExperienceUtils.getLevelExperience(level)

					val nextLevel = level + 1
					val nextLevelTotalXp = ExperienceUtils.getLevelExperience(nextLevel)
					val nextLevelRequiredXp = ExperienceUtils.getHowMuchExperienceIsLeftToLevelUp(profile.xp, nextLevel)

					val ranking = newSuspendedTransaction {
						GuildProfiles.select {
							GuildProfiles.guildId eq context.guild.idLong and
									(GuildProfiles.xp greaterEq profile.xp)
						}.count()
					}

					customTokens[Placeholders.EXPERIENCE_LEVEL.name] = level.toString()
					customTokens[Placeholders.EXPERIENCE_XP.name] = xp.toString()

					customTokens[Placeholders.EXPERIENCE_NEXT_LEVEL.name] = nextLevel.toString()
					customTokens[Placeholders.EXPERIENCE_NEXT_LEVEL_TOTAL_XP.name] = nextLevelTotalXp.toString()
					customTokens[Placeholders.EXPERIENCE_NEXT_LEVEL_REQUIRED_XP.name] = nextLevelRequiredXp.toString()
					customTokens[Placeholders.EXPERIENCE_RANKING.name] = ranking.toString()
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