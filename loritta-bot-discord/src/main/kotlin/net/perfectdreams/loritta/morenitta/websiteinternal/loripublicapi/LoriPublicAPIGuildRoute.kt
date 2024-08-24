package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.util.*
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNullById
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoint

abstract class LoriPublicAPIGuildRoute(
    m: LorittaBot,
    endpoint: LoriPublicHttpApiEndpoint,
    rateLimitOptions: RateLimitOptions
) : LoriPublicAPIRoute(m, endpoint, rateLimitOptions) {
    companion object {
        suspend fun validateGuildRequest(m: LorittaBot, tokenInfo: TokenInfo, guildId: Long): GuildAndMember {
            val guild = m.lorittaShards.getGuildById(guildId)
            if (guild == null) {
                throw WebsitePublicAPIException { call ->
                    call.respondJson(
                        "",
                        status = HttpStatusCode.NotFound
                    )
                }
            }

            val member = guild.retrieveMemberOrNullById(tokenInfo.userId)
            if (member == null) {
                throw WebsitePublicAPIException { call ->
                    call.respondJson(
                        "",
                        status = HttpStatusCode.NotFound
                    )
                }
            }

            return GuildAndMember(
                guild,
                member
            )
        }
    }

    override suspend fun onAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo) {
        val (guild, member) = validateGuildRequest(m, tokenInfo, call.parameters.getOrFail("guildId").toLong())
        onGuildAPIRequest(call, tokenInfo, guild, member)
    }

    abstract suspend fun onGuildAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo, guild: Guild, member: Member)

    data class GuildAndMember(
        val guild: Guild,
        val member: Member
    )
}