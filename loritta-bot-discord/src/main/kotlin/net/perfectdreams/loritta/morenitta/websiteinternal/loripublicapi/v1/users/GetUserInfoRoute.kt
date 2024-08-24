package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.users

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
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
            Profiles.innerJoin(UserSettings)
                .selectAll()
                .where {
                    Profiles.id eq userId
                }
                .limit(1)
                .firstOrNull()
        }

        if (result == null) {
            call.respondJson("", status = HttpStatusCode.NotFound)
            return
        }

        call.respondText(
            LoriPublicAPI.json.encodeToString(
                UserProfile(
                    result[Profiles.id].value,
                    result[Profiles.xp],
                    result[Profiles.money],
                    result[UserSettings.aboutMe],
                    result[UserSettings.gender],
                    result[UserSettings.emojiFightEmoji]
                )
            ),
            ContentType.Application.Json
        )
    }

    @Serializable
    data class UserProfile(
        @Serializable(LongAsStringSerializer::class)
        val id: Long,
        val xp: Long,
        val sonhos: Long,
        val aboutMe: String?,
        val gender: Gender,
        val emojiFightEmoji: String?
    )
}