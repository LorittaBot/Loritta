package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.guilds

import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatches
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightMatchmakingResults
import net.perfectdreams.loritta.cinnamon.pudding.tables.EmojiFightParticipants
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.*
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

class GetGuildUserEmojiFightVictoriesRoute(m: LorittaBot) : LoriPublicAPIGuildRoute(
    m,
    LoriPublicHttpApiEndpoints.EMOJIFIGHT_GUILD_VICTORIES,
    RateLimitOptions(
        5,
        5.seconds
    )
) {
    override suspend fun onGuildAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo, guild: Guild, member: Member) {
        if (!member.hasPermission(Permission.MANAGE_SERVER)) {
            call.respondJson("", status = HttpStatusCode.Unauthorized)
            return
        }

        val userId = call.parameters["userId"]?.toLongOrNull() ?: return
        val offset = call.request.queryParameters["offset"] ?.toLongOrNull() ?: 0
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
        if (limit !in 1..100) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Limit is not in valid range (1..100)"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val beforeDate = call.request.queryParameters["beforeDate"]?.let { Instant.parse(it) }
        val afterDate = call.request.queryParameters["afterDate"]?.let { Instant.parse(it) }
        val minimumEntryPrice = call.request.queryParameters["minimumEntryPrice"]?.toLongOrNull()
        if (minimumEntryPrice != null && 0 > minimumEntryPrice) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Invalid Minimum Entry Price"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        var query = Op.build {
            EmojiFightMatches.guild eq guild.idLong and (EmojiFightParticipants.user eq userId)
        }

        if (beforeDate != null)
            query = query and (EmojiFightMatches.finishedAt lessEq beforeDate)

        if (afterDate != null)
            query = query and (EmojiFightMatches.finishedAt greaterEq afterDate)

        if (minimumEntryPrice != null)
            query = query and (EmojiFightMatchmakingResults.entryPrice greaterEq minimumEntryPrice)

        val winCount = EmojiFightParticipants.user.count()
        val result = m.transaction {
            EmojiFightMatchmakingResults
                .innerJoin(EmojiFightMatches)
                .innerJoin(EmojiFightParticipants)
                .select(EmojiFightParticipants.user, winCount)
                .where(query)
                .groupBy(EmojiFightParticipants.user)
                .orderBy(winCount, SortOrder.DESC)
                .offset(offset).limit(limit)
                .toList()
                .firstOrNull()
        }

        if (result != null) {
            call.respondJson(
                LoriPublicAPI.json.encodeToString(
                    Result(
                        userId,
                        result[winCount]
                    )
                )
            )
        } else {
            call.respondJson(
                LoriPublicAPI.json.encodeToString(
                    Result(
                        userId,
                        0
                    )
                )
            )
        }
    }

    @Serializable
    data class Result(
        @Serializable(LongAsStringSerializer::class)
        val userId: Long,
        val victories: Long
    )
}