package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.guilds

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
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
import net.perfectdreams.loritta.serializable.GiveawayRoleExtraEntry
import java.awt.Color
import kotlin.time.Duration.Companion.seconds

class PutGiveawayRoute(m: LorittaBot) : LoriPublicAPIGuildRoute(
    m,
    LoriPublicHttpApiEndpoints.CREATE_GUILD_GIVEAWAY,
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
                if (!validateRole(call, guild, member, roleId, true))
                    return
            }
        }

        if (request.allowedRoles != null) {
            for (roleId in request.allowedRoles.roleIds) {
                if (!validateRole(call, guild, member, roleId, false))
                    return
            }
        }

        if (request.deniedRoles != null) {
            for (roleId in request.deniedRoles.roleIds) {
                if (!validateRole(call, guild, member, roleId, false))
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

        val extraEntries = mutableListOf<GiveawayRoleExtraEntry>()
        for (extraEntry in request.extraEntries) {
            if (extraEntries.any { it.roleId == extraEntry.roleId }) {
                call.respondJson(
                    Json.encodeToString(
                        GenericErrorResponse(
                            "There are two or more extra entries for the same role ID"
                        )
                    ),
                    status = HttpStatusCode.BadRequest
                )
                return
            }

            if (extraEntry.weight !in 2..100_000) {
                call.respondJson(
                    Json.encodeToString(
                        GenericErrorResponse(
                            "Extra entry weight is not in valid range (2..100000)"
                        )
                    ),
                    status = HttpStatusCode.BadRequest
                )
                return
            }

            extraEntries.add(
                GiveawayRoleExtraEntry(
                    extraEntry.roleId,
                    extraEntry.weight
                )
            )
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
            request.roleIdsToBeGivenToTheWinners?.map { it.toString() }?.ifEmpty { null },
            request.allowedRoles?.let {
                net.perfectdreams.loritta.serializable.GiveawayRoles(
                    it.roleIds,
                    it.isAndCondition
                )
            },
            request.deniedRoles?.let {
                net.perfectdreams.loritta.serializable.GiveawayRoles(
                    it.roleIds,
                    it.isAndCondition
                )
            },
            false,
            null,
            null,
            null,
            null,
            extraEntries,
            request.extraEntriesShouldStack
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
    private suspend fun validateRole(
        call: ApplicationCall,
        guild: Guild,
        invoker: Member,
        roleId: Long,
        checkIfCanInteract: Boolean
    ): Boolean {
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

        if (checkIfCanInteract) {
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
        }

        return true
    }

    @Serializable
    data class SpawnGiveawayRequest(
        @LoriPublicAPIParameter
        val channelId: Long,
        @LoriPublicAPIParameter
        val reason: String,
        @LoriPublicAPIParameter
        val description: String,
        @LoriPublicAPIParameter
        val imageUrl: String? = null,
        @LoriPublicAPIParameter
        val thumbnailUrl: String? = null,
        @LoriPublicAPIParameter
        @Serializable(AWTColorSerializer::class)
        val color: Color? = null,
        @LoriPublicAPIParameter
        val reaction: String,
        @LoriPublicAPIParameter
        val endsAt: Instant,
        @LoriPublicAPIParameter
        val numberOfWinners: Int,
        @LoriPublicAPIParameter
        val roleIdsToBeGivenToTheWinners: List<Long>? = null,
        @LoriPublicAPIParameter
        val allowedRoles: GiveawayRoles? = null,
        @LoriPublicAPIParameter
        val deniedRoles: GiveawayRoles? = null,
        @LoriPublicAPIParameter
        val extraEntries: List<GiveawayRoleExtraEntry> = emptyList(),
        @LoriPublicAPIParameter
        val extraEntriesShouldStack: Boolean = false
    ) {
        @Serializable
        data class GiveawayRoles(
            @LoriPublicAPIParameter
            val roleIds: List<Long>,
            @LoriPublicAPIParameter
            val isAndCondition: Boolean
        )

        @Serializable
        data class GiveawayRoleExtraEntry(
            @LoriPublicAPIParameter
            val roleId: Long,
            @LoriPublicAPIParameter
            val weight: Int
        )
    }

    @Serializable
    data class SpawnGiveawayResponse(
        val giveawayId: Long,
        @Serializable(LongAsStringSerializer::class)
        val messageId: Long
    )
}