package net.perfectdreams.loritta.cinnamon.platform

import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.request.KtorRequestException
import dev.kord.rest.service.RestClient
import io.ktor.client.*
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.StoredGenericInteractionData
import net.perfectdreams.loritta.cinnamon.platform.utils.UserUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.config.DiscordInteractionsConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.config.LorittaDiscordConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.config.ServicesConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

/**
 * Represents a Loritta Morenitta (Cinnamon) implementation.
 *
 * This should be extended by other modules :3
 */
abstract class LorittaCinnamon(
    val config: LorittaConfig,
    val discordConfig: LorittaDiscordConfig,
    val interactionsConfig: DiscordInteractionsConfig,
    val servicesConfig: ServicesConfig,

    val languageManager: LanguageManager,
    val services: Pudding,
    val http: HttpClient
) {
    // TODO: *Really* set a random seed
    val random = Random(0)

    val rest = RestClient(discordConfig.token)

    /**
     * Gets the current registered commands count
     */
    // The reason this is abstract is because the CommandManager class is in the "commands" module, so we can't access it from here
    abstract fun getCommandCount(): Int

    suspend fun getCachedUserInfo(userId: UserId): CachedUserInfo? {
        // First, try getting the cached user info from the database
        val cachedUserInfoFromDatabase = services.users.getCachedUserInfoById(userId)
        if (cachedUserInfoFromDatabase != null)
            return cachedUserInfoFromDatabase

        // If not present, get it from Discord!
        val restUser = try {
            rest.user.getUser(Snowflake(userId.value))
        } catch (e: KtorRequestException) {
            null
        }

        if (restUser != null) {
            // If the REST user really exists, then let's update it in our database and then return the cached user info
            services.users.insertOrUpdateCachedUserInfo(
                UserId(restUser.id.value),
                restUser.username,
                restUser.discriminator,
                restUser.avatar
            )

            return CachedUserInfo(
                UserId(restUser.id.value),
                restUser.username,
                restUser.discriminator,
                restUser.avatar
            )
        }

        return null
    }

    suspend fun insertOrUpdateCachedUserInfo(user: User) {
        services.users.insertOrUpdateCachedUserInfo(
            UserId(user.id.value),
            user.name,
            user.discriminator,
            user.avatarHash
        )
    }

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
     * Encodes the [data] to fit on a button. If it doesn't fit in a button, a [StoredGenericInteractionData] will be encoded instead and the data will be stored on the database.
     */
    @OptIn(ExperimentalSerializationApi::class)
    suspend inline fun <reified T> encodeDataForComponentOrStoreInDatabase(data: T): String {
        val encoded = ComponentDataUtils.encode(data)

        // Let's suppose that all components always have 5 characters at the start
        // (Technically it is true: Discord InteraKTions uses ":" as the separator, and we only use 4 chars for ComponentExecutorIds)
        val padStart = 5 // "0000:"

        if (100 - padStart >= encoded.length) {
            // Can fit on a button! So let's just return what we currently have
            return encoded
        } else {
            // Can't fit on a button... Let's store it on the database!
            val now = Clock.System.now()

            val interactionDataId = services.interactionsData.insertInteractionData(
                Json.encodeToJsonElement<T>(
                    data
                ).jsonObject,
                now,
                now + 15.minutes
            )

            return ComponentDataUtils.encode(StoredGenericInteractionData(interactionDataId))
        }
    }

    /**
     * Decodes the [data] based on the source data:
     * * If [data] is a [StoredGenericInteractionData], the data will be retrieved from the database and deserialized using [T]
     * * If else, the data will be deserialized using [T]
     *
     * This should be used in conjuction with [encodeDataForComponentOrStoreInDatabase]
     */
    @OptIn(ExperimentalSerializationApi::class)
    suspend inline fun <reified T> decodeDataFromComponentOrFromDatabase(data: String): T? {
        val genericInteractionData = try {
            ComponentDataUtils.decode<StoredGenericInteractionData>(data)
        } catch (e: SerializationException) {
            // If the deserialization failed, then let's try deserializing as T
            return ComponentDataUtils.decode<T>(data)
        }

        val dataFromDatabase = services.interactionsData.getInteractionData(genericInteractionData.interactionDataId)
            ?.jsonObject ?: return null

        return Json.decodeFromJsonElement<T>(dataFromDatabase)
    }
}