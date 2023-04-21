package net.perfectdreams.loritta.morenitta.website.routes.api.v1.callbacks

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.UserSnowflake
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferGuildMembers
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferSuccessfulVerifications
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.GamerSaferUserRoles
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.gamersafer.*
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.util.*

class PostGamerSaferCallbackRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/callbacks/gamersafer") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val eventJson = Json {
		ignoreUnknownKeys = true
		classDiscriminator = "event"
	}

	override suspend fun onRequest(call: ApplicationCall) {
		logger.info { "Received GamerSafer callback!" }
		val event = eventJson.decodeFromString<GamerSaferEvent>(call.receiveText().also { logger.info { it } })

		when (event) {
			is GuildInviteEvent -> {
				val (providerId, guildId, additionalDataAsJsonBase64) = event.payload.internalId.split("|||")
				val additionalData = Json.decodeFromString<GamerSaferGuildInviteAdditionalData>(Base64.getDecoder().decode(additionalDataAsJsonBase64.toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8))

				if (additionalData.token != loritta.config.loritta.gamerSafer.webhookSecret) {
					call.respondText("", status = HttpStatusCode.Unauthorized)
					return
				}

				loritta.transaction {
					GamerSaferGuildMembers.insert {
						it[GamerSaferGuildMembers.guild] = guildId.toLong()
						it[GamerSaferGuildMembers.discordUser] = additionalData.userId
						it[GamerSaferGuildMembers.gamerSaferUser] = event.payload.guildMemberId
					}
				}
			}
			is PlayerVerificationEvent -> {
				val additionalData = Json.decodeFromString<GamerSaferPlayerVerificationAdditionalData>(Base64.getDecoder().decode(event.payload.discordMessage.toByteArray(Charsets.UTF_8)).toString(Charsets.UTF_8))

				if (additionalData.token != loritta.config.loritta.gamerSafer.webhookSecret) {
					call.respondText("", status = HttpStatusCode.Unauthorized)
					return
				}

				// Check what roles the user can receive
				val matchedUserRoles = loritta.transaction {
					// Insert that the user has successfully verified
					GamerSaferSuccessfulVerifications.insert {
						it[GamerSaferSuccessfulVerifications.guild] = additionalData.guildId
						it[GamerSaferSuccessfulVerifications.user] = additionalData.userId
						it[GamerSaferSuccessfulVerifications.verifiedAt] = Instant.now()
					}

					GamerSaferUserRoles.select {
						GamerSaferUserRoles.user eq (additionalData.userId) and (GamerSaferUserRoles.guild eq additionalData.guildId)
					}.toList()
				}

				val guild = loritta.lorittaShards.getGuildById(additionalData.guildId)!!

				for (matchedUserRole in matchedUserRoles) {
					val role = guild.getRoleById(matchedUserRole[GamerSaferUserRoles.role]) ?: continue // Role does not exist!
					guild.addRoleToMember(UserSnowflake.fromId(additionalData.userId), role)
						.await()
				}

				// Done!
			}
		}

		call.respondText("{}")
	}
}