package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.guilds

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.AWTColorSerializer
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.*
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints
import net.perfectdreams.loritta.serializable.GiveawayRoles
import java.awt.Color
import kotlin.time.Duration.Companion.seconds

class PutGiveawayRoute(m: LorittaBot) : LoriPublicAPIGuildRoute(
    m,
    LoriPublicHttpApiEndpoints.REROLL_GUILD_GIVEAWAY,
    RateLimitOptions(
        2,
        5.seconds
    )
) {
    override suspend fun onGuildAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo, guild: Guild, member: Member) {
        if (!member.hasPermission(Permission.MESSAGE_MANAGE)) {
            call.respondJson("", status = HttpStatusCode.Unauthorized)
            return
        }

        val serverConfig = m.getOrCreateServerConfig(guild.idLong)
        val baseLocale = m.localeManager.getLocaleById(serverConfig.localeId)
        val i18nContext = m.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

        val request = Json.decodeFromString<SpawnGiveawayRequest>(call.receiveText())
        val channel = guild.getGuildMessageChannelById(request.channelId)
        if (channel == null) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Unknown Channel"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        if (!channel.canTalk()) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Loritta does not have permission to view and send messages on that channel"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        if (!channel.canTalk(member)) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "You don't have permission to view and send messages on that channel"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val lorittaAsMember = guild.selfMember

        if (!lorittaAsMember.hasPermission(channel, Permission.MESSAGE_ADD_REACTION)) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Loritta does not have permission to react on that channel"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        if (!lorittaAsMember.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Loritta does not have permission to send embeds on that channel"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        if (!lorittaAsMember.hasPermission(channel, Permission.MESSAGE_HISTORY)) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Loritta does not have permission to see the channel history on that channel"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        if (request.numberOfWinners !in 1..100) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Number of Winners is not in valid range (1..100)"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        if (request.roleIdsToBeGivenToTheWinners != null) {
            for (roleId in request.roleIdsToBeGivenToTheWinners) {
                if (!validateRole(call, guild, member, roleId))
                    return
            }
        }

        if (request.allowedRoles != null) {
            for (roleId in request.allowedRoles.roleIds) {
                if (!validateRole(call, guild, member, roleId))
                    return
            }
        }

        if (request.deniedRoles != null) {
            for (roleId in request.deniedRoles.roleIds) {
                if (!validateRole(call, guild, member, roleId))
                    return
            }
        }

        if (Clock.System.now() >= request.endsAt) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Giveaway end time is in the past"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }


        val createdGiveaway = m.giveawayManager.spawnGiveaway(
            baseLocale,
            i18nContext,
            channel,
            request.reason,
            request.description,
            request.imageUrl,
            request.thumbnailUrl,
            request.color,
            request.reaction,
            request.endsAt.toEpochMilliseconds(),
            request.numberOfWinners,
            null,
            request.roleIdsToBeGivenToTheWinners?.map { it.toString() },
            request.allowedRoles,
            request.deniedRoles,
            false,
            null,
            null,
            null,
            null
        )

        call.respondJson(
            LoriPublicAPI.json.encodeToString(
                SpawnGiveawayResponse(
                    createdGiveaway.id.value,
                    createdGiveaway.messageId
                )
            ),
            status = HttpStatusCode.Created
        )
    }

    /**
     * Validates if the [roleId] exists and if it is interactable both by Loritta and by the user that invoked it
     */
    private suspend fun validateRole(call: ApplicationCall, guild: Guild, invoker: Member, roleId: Long): Boolean {
        val role = guild.getRoleById(roleId)
        if (role == null) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Unknown Role $roleId"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return false
        }

        if (!guild.selfMember.canInteract(role) || role.isManaged) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Loritta can't interact with the role $roleId"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return false
        }

        if (!invoker.hasPermission(Permission.MANAGE_ROLES) || !invoker.canInteract(role)) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "You can't interact with the role $roleId"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return false
        }

        return true
    }

    @Serializable
    data class SpawnGiveawayRequest(
        val channelId: Long,
        val reason: String,
        val description: String,
        val imageUrl: String? = null,
        val thumbnailUrl: String? = null,
        @Serializable(AWTColorSerializer::class)
        val color: Color? = null,
        val reaction: String,
        val endsAt: Instant,
        val numberOfWinners: Int,
        val roleIdsToBeGivenToTheWinners: List<Long>? = null,
        val allowedRoles: GiveawayRoles? = null,
        val deniedRoles: GiveawayRoles? = null
    )

    @Serializable
    data class SpawnGiveawayResponse(
        val giveawayId: Long,
        @Serializable(LongAsStringSerializer::class)
        val messageId: Long
    )
}