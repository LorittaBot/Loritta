package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.sonhos

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.services.UsersService
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import kotlin.time.Duration.Companion.seconds

class GetRichestUsersRoute(m: LorittaBot) : LoriPublicAPIRoute(
    m,
    "/sonhos/rank",
    RateLimitOptions(
        5,
        5.seconds
    )
) {
    companion object {
        // TODO: Fix the total Count, however there isn't an easy way to fix this
        // While you could be thinking "well, I can just use a SELECT COUNT(*) FROM profiles query!", that gonna be very resource intensive on the db side.
        // Because Loritta has a looooooooot of profiles
        // (Besides, Loritta will always have more than (pageSize * RankingGenerator.VALID_RANKING_PAGES.last) profiles, heh
        private const val HARDCODED_OFFSET_LIMIT = 100_000
    }

    override suspend fun onAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo) {
        val limit = call.parameters["limit"]?.toInt() ?: 10
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
        val offset = (call.parameters["offset"]?.toLong()) ?: 0
        if (offset !in 0..100_000) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Offset is not in valid range (0..$HARDCODED_OFFSET_LIMIT)"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val profiles = m.pudding.transaction {
            val profiles = Profiles
                .select {
                    Profiles.id notInSubQuery UsersService.validBannedUsersList(System.currentTimeMillis())
                }
                .orderBy(Profiles.money, SortOrder.DESC)
                .limit(limit, offset)
                .toList()

            profiles
        }

        // We send the internal transaction because it would take veeeery long to implement all of them
        // Also because *technically* the "type" of the transaction can be used to filter transactions too (they are very useful for that)
        call.respondText(
            LoriPublicAPI.json.encodeToString(
                Result(
                    profiles.map {
                        Result.UserProfile(
                            it[Profiles.id].value,
                            it[Profiles.money]
                        )
                    },
                    Result.PartialPaging(
                        limit,
                        offset
                    )
                )
            ),
            ContentType.Application.Json
        )
    }

    @Serializable
    data class Result(
        val results: List<UserProfile>,
        val paging: PartialPaging
    ) {
        @Serializable
        data class UserProfile(
            @Serializable(LongAsStringSerializer::class)
            val id: Long,
            val sonhos: Long
        )

        @Serializable
        data class PartialPaging(
            val limit: Int,
            val offset: Long
        )
    }
}