package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.users

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.LongAsStringSerializer
import kotlinx.serialization.encodeToString
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserSettings
import net.perfectdreams.loritta.common.utils.Gender
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.LoriPublicAPI
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.LoriPublicAPIRoute
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.RateLimitOptions
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.TokenInfo
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints
import net.perfectdreams.loritta.serializable.UserBannedState
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import kotlin.time.Duration.Companion.seconds

class GetUserInfoRoute(m: LorittaBot) : LoriPublicAPIRoute(
    m,
    LoriPublicHttpApiEndpoints.GET_USER_BY_ID,
    RateLimitOptions(
        5,
        5.seconds
    )
) {
    override suspend fun onAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo) {
        val userId = call.parameters.getOrFail("userId").toLong()

        val result = m.transaction {
            val profile = Profiles.innerJoin(UserSettings)
                .selectAll()
                .where {
                    Profiles.id eq userId
                }
                .limit(1)
                .firstOrNull()

            if (profile == null)
                return@transaction Result.NotFound

            val bannedState = m.pudding.users.getUserBannedState(UserId(userId))

            return@transaction Result.Success(profile, bannedState)
        }

        when (result) {
            Result.NotFound -> {
                call.respondJson("", status = HttpStatusCode.NotFound)
                return
            }

            is Result.Success -> {
                call.respondText(
                    LoriPublicAPI.json.encodeToString(
                        UserProfile(
                            result.profile[Profiles.id].value,
                            result.profile[Profiles.xp],
                            result.profile[Profiles.money],
                            result.profile[UserSettings.aboutMe],
                            result.profile[UserSettings.gender],
                            result.profile[UserSettings.emojiFightEmoji],
                            result.state?.let {
                                UserProfile.LorittaBanState(
                                    it.bannedAt,
                                    it.expiresAt,
                                    it.reason
                                )
                            }
                        )
                    ),
                    ContentType.Application.Json
                )
            }
        }
    }

    @Serializable
    data class UserProfile(
        @Serializable(LongAsStringSerializer::class)
        val id: Long,
        val xp: Long,
        val sonhos: Long,
        val aboutMe: String?,
        val gender: Gender,
        val emojiFightEmoji: String?,
        val lorittaBanState: LorittaBanState?
    ) {
        @Serializable
        data class LorittaBanState(
            val bannedAt: Instant,
            val expiresAt: Instant?,
            val reason: String,
        )
    }

    private sealed class Result {
        data class Success(val profile: ResultRow, val state: UserBannedState?) : Result()
        data object NotFound : Result()
    }
}