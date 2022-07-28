package net.perfectdreams.loritta.cinnamon.discord

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import io.ktor.client.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.UserUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.config.DiscordInteractionsConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.config.LorittaDiscordConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.config.ServicesConfig
import net.perfectdreams.loritta.cinnamon.discord.utils.correios.CorreiosClient
import net.perfectdreams.loritta.cinnamon.discord.utils.ecb.ECBManager
import net.perfectdreams.loritta.cinnamon.discord.utils.falatron.FalatronModelsManager
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.LorittaNotification
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.LorittaVoiceConnectionStateRequest
import net.perfectdreams.loritta.cinnamon.pudding.data.notifications.LorittaVoiceConnectionStateResponse
import net.perfectdreams.loritta.cinnamon.pudding.utils.LorittaNotificationListener
import net.perfectdreams.minecraftmojangapi.MinecraftMojangAPI
import net.perfectdreams.randomroleplaypictures.client.RandomRoleplayPicturesClient
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Represents a Loritta Morenitta (Cinnamon) implementation.
 *
 * This should be extended by other modules :3
 */
abstract class LorittaCinnamon(
    val config: LorittaConfig,
    discordConfig: LorittaDiscordConfig,
    val interactionsConfig: DiscordInteractionsConfig,
    val servicesConfig: ServicesConfig,

    val languageManager: LanguageManager,
    services: Pudding,
    val http: HttpClient
) : LorittaDiscordStuff(discordConfig, services) {
    val gabrielaImageServerClient = GabrielaImageServerClient(
        servicesConfig.gabrielaImageServer.url,
        HttpClient {
            // Increase the default timeout for image generation, because some video generations may take too long to be generated
            install(HttpTimeout) {
                this.socketTimeoutMillis = 60_000
                this.requestTimeoutMillis = 60_000
                this.connectTimeoutMillis = 60_000
            }
        }
    )

    val mojangApi = MinecraftMojangAPI()
    val correiosClient = CorreiosClient()
    val randomRoleplayPicturesClient = RandomRoleplayPicturesClient(servicesConfig.randomRoleplayPictures.url)
    val falatronModelsManager = FalatronModelsManager().also {
        it.startUpdater()
    }
    val ecbManager = ECBManager()

    // TODO: *Really* set a random seed
    val random = Random(0)

    val notificationListener = LorittaNotificationListener(services)
        .apply {
            this.start()
        }

    /**
     * Gets the current registered commands count
     */
    // The reason this is abstract is because the CommandManager class is in the "commands" module, so we can't access it from here
    abstract fun getCommandCount(): Int

    /**
     * Sends the [builder] message to the [userId] via the user's direct message channel.
     *
     * The ID of the direct message channel is cached.
     */
    suspend fun sendMessageToUserViaDirectMessage(userId: UserId, builder: UserMessageCreateBuilder.() -> (Unit)) = UserUtils.sendMessageToUserViaDirectMessage(
        services,
        rest,
        userId,
        builder
    )

    /**
     * Validates Loritta's voice state in [guildId] for [userId]
     */
    suspend fun validateVoiceState(guildId: Snowflake, userId: Snowflake): VoiceStateValidationResult {
        val userConnectedVoiceChannelId = cache.getUserConnectedVoiceChannel(guildId, userId) ?: return UserNotConnectedToAVoiceChannel

        // Can we talk there?
        if (!cache.lorittaHasPermission(guildId, userConnectedVoiceChannelId, Permission.Connect, Permission.Speak))
            return LorittaDoesntHavePermissionToTalkOnChannel(userConnectedVoiceChannelId) // Looks like we can't...

        // Are we already playing something in another channel already?
        val voiceConnectionStatus = getLorittaVoiceConnectionStateOrNull(guildId) ?: return VoiceStateTimeout // Looks like something went wrong! Took too long to get if I'm in a voice channel or not
        val lorittaConnectedVoiceChannelId = voiceConnectionStatus.channelId?.let { Snowflake(it) }
        if (voiceConnectionStatus.playing && lorittaConnectedVoiceChannelId != null && lorittaConnectedVoiceChannelId != userConnectedVoiceChannelId)
            return AlreadyPlayingInAnotherChannel(
                userConnectedVoiceChannelId,
                lorittaConnectedVoiceChannelId
            )

        return VoiceStateValidationData(
            userConnectedVoiceChannelId,
            lorittaConnectedVoiceChannelId
        )
    }

    sealed class VoiceStateValidationResult
    object UserNotConnectedToAVoiceChannel : VoiceStateValidationResult()
    class LorittaDoesntHavePermissionToTalkOnChannel(val userConnectedVoiceChannel: Snowflake) : VoiceStateValidationResult()
    object VoiceStateTimeout : VoiceStateValidationResult()
    class AlreadyPlayingInAnotherChannel(
        val userConnectedVoiceChannel: Snowflake,
        val lorittaConnectedVoiceChannel: Snowflake
    ) : VoiceStateValidationResult()
    data class VoiceStateValidationData(
        val userConnectedVoiceChannel: Snowflake,
        val lorittaConnectedVoiceChannel: Snowflake?
    ) : VoiceStateValidationResult()

    /**
     * Gets Loritta's coroutine state in [guildId], waits [timeout] until timing out.
     *
     * @param guildId the guild id
     * @param timeout how much time we will wait for a response
     * @return a [LorittaVoiceConnectionStateResponse] or null if it the request was timed out
     */
    suspend fun getLorittaVoiceConnectionStateOrNull(guildId: Snowflake, timeout: Duration = 5.seconds): LorittaVoiceConnectionStateResponse? {
        return coroutineScope {
            val uniqueNotificationId = UUID.randomUUID().toString()

            val voiceConnectionStatusDeferred = async {
                withTimeoutOrNull(timeout) {
                    filterNotificationsByUniqueId(uniqueNotificationId)
                        .filterIsInstance<LorittaVoiceConnectionStateResponse>()
                        .first()
                }
            }

            services.notify(
                LorittaVoiceConnectionStateRequest(
                    uniqueNotificationId,
                    guildId.toLong()
                )
            )

            voiceConnectionStatusDeferred.await()
        }
    }

    /**
     * Filters received notifications by their [notificationUniqueId]
     *
     * @param notificationUniqueId the notification unique ID
     * @return a flow containing only notifications that match the unique ID
     */
    fun filterNotificationsByUniqueId(notificationUniqueId: String): Flow<LorittaNotification> {
        return notificationListener.notifications.filterIsInstance<LorittaNotification>()
            .filter { it.uniqueId == notificationUniqueId }
    }
}