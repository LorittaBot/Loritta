package net.perfectdreams.loritta.cinnamon.discord.voice

import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.voice.VoiceConnection
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.DiscordGatewayEventsProcessorMetrics
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway
import net.perfectdreams.loritta.morenitta.LorittaBot
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages Loritta's voice connections
 */
class LorittaVoiceConnectionManager(val loritta: LorittaBot) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val voiceConnections = ConcurrentHashMap<Snowflake, LorittaVoiceConnection>()
    private val voiceConnectionsMutexes = ConcurrentHashMap<Snowflake, Mutex>()

    /**
     * Gets or creates a [LorittaVoiceConnection] on the [guildId] and [channelId]
     *
     * @param guildId the guild's ID
     * @param channelId the channel's ID
     * @return a [LorittaVoiceConnection] instance
     */
    suspend fun getOrCreateVoiceConnection(
        guildId: Snowflake,
        channelId: Snowflake
    ): LorittaVoiceConnection =
        getOrCreateVoiceConnection(loritta.deviousShards.getGatewayForGuild(guildId), guildId, channelId)

    /**
     * Gets or creates a [LorittaVoiceConnection] on the [guildId] and [channelId]
     *
     * @param gateway the gateway connection
     * @param guildId the guild's ID
     * @param channelId the channel's ID
     * @return a [LorittaVoiceConnection] instance
     */
    @OptIn(KordVoice::class)
    suspend fun getOrCreateVoiceConnection(
        gateway: DeviousGateway,
        guildId: Snowflake,
        channelId: Snowflake
    ): LorittaVoiceConnection {
        voiceConnectionsMutexes.getOrPut(guildId) { Mutex() }.withLock {
            val lorittaVoiceConnection = voiceConnections[guildId]
            if (lorittaVoiceConnection != null)
                return lorittaVoiceConnection

            val notificationChannel = Channel<Unit>()

            // TODO: Send a UpdateVoiceState to disconnect Loritta from any voice channel, useful if our cache doesn't match the "reality"
            val audioProvider = LorittaAudioProvider(notificationChannel)

            val vc = VoiceConnection(
                gateway.kordGateway,
                loritta.config.loritta.discord.applicationId,
                channelId,
                guildId
            ) {
                audioProvider(audioProvider)
            }

            vc.connect()

            val loriVC = LorittaVoiceConnection(gateway, guildId, channelId, vc, audioProvider, notificationChannel)
            voiceConnections[guildId] = loriVC

            loriVC.launchAudioClipRequestsJob()

            // Clean up voice connection after it is shutdown
            vc.scope.launch {
                try {
                    awaitCancellation()
                } finally {
                    shutdownVoiceConnection(guildId, loriVC)
                }
            }

            DiscordGatewayEventsProcessorMetrics.voiceConnections
                .set(voiceConnections.size.toDouble())

            return loriVC
        }
    }

    /**
     * Shutdowns the [VoiceConnection] and removes it from the [voiceConnections] map
     *
     * While [VoiceConnection] has a [VoiceConnection.shutdown] method, *it shouldn't be used directly to avoid memory leaks*!
     *
     * @param guildId the guild ID
     * @param voiceConnection the voice connection that will be shutdown
     */
    suspend fun shutdownVoiceConnection(guildId: Snowflake, voiceConnection: LorittaVoiceConnection) {
        logger.info { "Shutting down voice connecion $voiceConnection related to $guildId" }
        voiceConnections.remove(guildId, voiceConnection)
        voiceConnection.shutdown()

        DiscordGatewayEventsProcessorMetrics.voiceConnections
            .set(voiceConnections.size.toDouble())
    }

    /**
     * Validates Loritta's voice state in [guildId] for [userId]
     */
    suspend fun validateVoiceState(guildId: Snowflake, userId: Snowflake): VoiceStateValidationResult {
        val userConnectedVoiceChannelId = loritta.cache.getUserConnectedVoiceChannel(guildId, userId)
            ?: return VoiceStateValidationResult.UserNotConnectedToAVoiceChannel

        // Can we talk there?
        if (!loritta.cache.lorittaHasPermission(
                guildId,
                userConnectedVoiceChannelId,
                Permission.Connect,
                Permission.Speak
            )
        )
            return VoiceStateValidationResult.LorittaDoesntHavePermissionToTalkOnChannel(userConnectedVoiceChannelId) // Looks like we can't...

        // Are we already playing something in another channel already?
        val currentlyActiveVoiceConnection = voiceConnections[guildId]

        if (currentlyActiveVoiceConnection != null) {
            if (currentlyActiveVoiceConnection.isPlaying() && currentlyActiveVoiceConnection.channelId != userConnectedVoiceChannelId)
                return VoiceStateValidationResult.AlreadyPlayingInAnotherChannel(
                    userConnectedVoiceChannelId,
                    currentlyActiveVoiceConnection.channelId
                )
        }

        return VoiceStateValidationResult.VoiceStateValidationData(
            userConnectedVoiceChannelId,
            currentlyActiveVoiceConnection?.channelId
        )
    }

    sealed class VoiceStateValidationResult {
        object UserNotConnectedToAVoiceChannel : VoiceStateValidationResult()
        class LorittaDoesntHavePermissionToTalkOnChannel(val userConnectedVoiceChannel: Snowflake) :
            VoiceStateValidationResult()

        class AlreadyPlayingInAnotherChannel(
            val userConnectedVoiceChannel: Snowflake,
            val lorittaConnectedVoiceChannel: Snowflake
        ) : VoiceStateValidationResult()

        data class VoiceStateValidationData(
            val userConnectedVoiceChannel: Snowflake,
            val lorittaConnectedVoiceChannel: Snowflake?
        ) : VoiceStateValidationResult()
    }
}