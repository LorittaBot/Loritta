package net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.v1.guilds

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.musicalchairs.MusicalChairsManager
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.morenitta.websiteinternal.loripublicapi.*
import net.perfectdreams.loritta.publichttpapi.LoriPublicHttpApiEndpoints
import kotlin.time.Duration.Companion.seconds

class PostMusicalChairsRoute(m: LorittaBot) : LoriPublicAPIGuildRoute(
    m,
    LoriPublicHttpApiEndpoints.CREATE_GUILD_MUSICALCHAIRS,
    RateLimitOptions(
        2,
        5.seconds
    )
) {
    override suspend fun onGuildAPIRequest(call: ApplicationCall, tokenInfo: TokenInfo, guild: Guild, member: Member) {
        if (!member.hasPermission(Permission.VOICE_MOVE_OTHERS)) {
            call.respondJson("", status = HttpStatusCode.Unauthorized)
            return
        }

        val serverConfig = m.getOrCreateServerConfig(guild.idLong)
        val baseLocale = m.localeManager.getLocaleById(serverConfig.localeId)
        val i18nContext = m.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

        val request = Json.decodeFromString<SpawnMusicalChairsRequest>(call.receiveText())
        val voiceChannel = guild.getChannelById(AudioChannel::class.java, request.voiceChannelId)
        if (voiceChannel == null) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Unknown Audio Channel"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        val channel = guild.getGuildMessageChannelById(request.messageChannelId)
        if (channel == null) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Unknown Message Channel"
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

        val validParticipants = voiceChannel.members.filter { !it.user.isBot }
        if (1 >= validParticipants.size) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "You need at least two users on the channel to start the event!"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        if (m.musicalChairsManager.musicalChairsSessions.contains(guild.idLong)) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "You need to wait the game to finish before starting a new game!"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        if (!guild.selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK)) {
            call.respondJson(
                Json.encodeToString(
                    GenericErrorResponse(
                        "Loritta does not have permission to connect and speak on that channel"
                    )
                ),
                status = HttpStatusCode.BadRequest
            )
            return
        }

        m.musicalChairsManager.startMusicalChairs(
            MusicalChairsManager.MusicalChairsContext.MusicalDirectContext(
                i18nContext,
                channel
            ),
            i18nContext,
            guild,
            voiceChannel,
            voiceChannel.members.filter { !it.user.isBot },
            true,
            0,
            if (request.songId != null) { m.musicalChairsManager.songs.first { it.name == request.songId } } else m.musicalChairsManager.songs.random(),
            Mutex(),
            1
        )

        call.respondJson(
            "",
            status = HttpStatusCode.NoContent
        )
    }

    @Serializable
    data class SpawnMusicalChairsRequest(
        @LoriPublicAPIParameter
        val voiceChannelId: Long,
        @LoriPublicAPIParameter
        val messageChannelId: Long,
        @LoriPublicAPIParameter
        val songId: String? = null
    )
}