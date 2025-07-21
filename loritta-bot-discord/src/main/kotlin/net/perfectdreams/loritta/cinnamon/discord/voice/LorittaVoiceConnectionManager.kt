package net.perfectdreams.loritta.cinnamon.discord.voice

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.morenitta.LorittaBot
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages Loritta's voice connections
 */
class LorittaVoiceConnectionManager(val loritta: LorittaBot) {
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

    val voiceConnections = ConcurrentHashMap<Long, LorittaVoiceConnection>()
    val voiceConnectionsMutexes = ConcurrentHashMap<Long, Mutex>()

    /**
     * Gets or creates a [LorittaVoiceConnection] on the [guildId] and [channelId]
     *
     * @param guildId the guild's ID
     * @param channelId the channel's ID
     * @return a [LorittaVoiceConnection] instance
     */
    suspend fun getOrCreateVoiceConnection(
        guildId: Long,
        channelId: Long
    ): LorittaVoiceConnection {
        voiceConnectionsMutexes.getOrPut(guildId) { Mutex() }.withLock {
            val lorittaVoiceConnection = voiceConnections[guildId]
            if (lorittaVoiceConnection != null) {
                // Switch to new channel (if it is a new channel)
                lorittaVoiceConnection.switchChannel(channelId)
                return lorittaVoiceConnection
            }

            val notificationChannel = Channel<Unit>()

            val guild = loritta.lorittaShards.getGuildById(guildId)!!

            // TODO: Send a UpdateVoiceState to disconnect Loritta from any voice channel, useful if our cache doesn't match the "reality"
            val audioProvider = LorittaAudioProvider(notificationChannel)

            val audioManager = guild.audioManager
            audioManager.sendingHandler = audioProvider
            guild.audioManager.openAudioConnection(guild.getVoiceChannelById(channelId)!!)

            val loriVC = LorittaVoiceConnection(guild, channelId, audioManager, audioProvider, notificationChannel)
            voiceConnections[guildId] = loriVC

            loriVC.launchAudioClipRequestsJob()

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
    suspend fun shutdownVoiceConnection(guildId: Long, voiceConnection: LorittaVoiceConnection) {
        logger.info { "Shutting down voice connection $voiceConnection related to $guildId" }
        voiceConnections.remove(guildId, voiceConnection)
        voiceConnection.shutdown()
    }

    /**
     * Validates Loritta's voice state in [guildId] for [userId]
     */
    suspend fun validateVoiceState(guildId: Long, userId: Long): VoiceStateValidationResult {
        val userConnectedVoiceChannel = loritta.cache.getUserConnectedVoiceChannel(guildId, userId) ?: return VoiceStateValidationResult.UserNotConnectedToAVoiceChannel

        // Can we talk there?
        if (!userConnectedVoiceChannel.guild.selfMember.hasPermission(userConnectedVoiceChannel, net.dv8tion.jda.api.Permission.VOICE_CONNECT, net.dv8tion.jda.api.Permission.VOICE_SPEAK))
            return VoiceStateValidationResult.LorittaDoesntHavePermissionToTalkOnChannel(userConnectedVoiceChannel.idLong) // Looks like we can't...

        // Are we already playing something in another channel already?
        val currentlyActiveVoiceConnection = voiceConnections[guildId]

        if (currentlyActiveVoiceConnection != null) {
            if (currentlyActiveVoiceConnection.isPlaying() && currentlyActiveVoiceConnection.channelId.toLong() != userConnectedVoiceChannel.idLong)
                return VoiceStateValidationResult.AlreadyPlayingInAnotherChannel(
                    userConnectedVoiceChannel.idLong,
                    currentlyActiveVoiceConnection.channelId
                )
        }

        return VoiceStateValidationResult.VoiceStateValidationData(
            userConnectedVoiceChannel.idLong,
            currentlyActiveVoiceConnection?.channelId
        )
    }

    sealed class VoiceStateValidationResult {
        object UserNotConnectedToAVoiceChannel : VoiceStateValidationResult()
        class LorittaDoesntHavePermissionToTalkOnChannel(val userConnectedVoiceChannel: Long) : VoiceStateValidationResult()
        class AlreadyPlayingInAnotherChannel(
            val userConnectedVoiceChannel: Long,
            val lorittaConnectedVoiceChannel: Long
        ) : VoiceStateValidationResult()
        data class VoiceStateValidationData(
            val userConnectedVoiceChannel: Long,
            val lorittaConnectedVoiceChannel: Long?
        ) : VoiceStateValidationResult()
    }
}