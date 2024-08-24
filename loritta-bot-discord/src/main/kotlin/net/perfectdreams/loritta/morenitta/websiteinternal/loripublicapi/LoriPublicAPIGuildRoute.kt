package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.util.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNullById
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson

abstract class LoriPublicAPIGuildRoute(
    m: LorittaBot,
    path: String,
    rateLimitOptions: RateLimitOptions
) : LoriPublicAPIRoute(m, path, rateLimitOptions) {
    override suspend fun onAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo) {
        val guild = m.lorittaShards.getGuildById(call.parameters.getOrFail("guildId"))
        if (guild == null) {
            call.respondJson(
                "",
                status = HttpStatusCode.NotFound
            )
            return
        }

        val member = guild.retrieveMemberOrNullById(tokenInfo.userId)
        if (member == null) {
            call.respondJson(
                "",
                status = HttpStatusCode.NotFound
            )
            return
        }

        onGuildAPIRequest(call, tokenInfo, guild, member)
    }

    abstract suspend fun onGuildAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo, guild: Guild, member: Member)
}