package net.perfectdreams.loritta.cinnamon.platform

import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import io.ktor.client.*
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.common.utils.config.LorittaConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.UserUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.config.DiscordInteractionsConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.config.LorittaDiscordConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.config.ServicesConfig
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import kotlin.random.Random

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
    // TODO: *Really* set a random seed
    val random = Random(0)

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
}