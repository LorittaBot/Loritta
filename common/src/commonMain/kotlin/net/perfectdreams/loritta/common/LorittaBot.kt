package net.perfectdreams.loritta.common

import io.ktor.client.*
import net.perfectdreams.loritta.common.emotes.EmoteManager
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.api.commands.Command
import net.perfectdreams.loritta.common.api.commands.CommandContext
import net.perfectdreams.loritta.common.api.commands.CommandMap
import net.perfectdreams.loritta.common.utils.LorittaAssets
import kotlin.random.Random

/**
 * Represents a Loritta Morenitta implementation.
 *
 * This should be extended by plataform specific Lori's :3
 */
abstract class LorittaBot {
    abstract val commandMap: CommandMap<Command<CommandContext>>
    abstract val assets: LorittaAssets
    abstract val http: HttpClient
    abstract val httpWithoutTimeout: HttpClient

    // TODO: *Really* set a random seed
    // TODO: Maybe it would be better if this wasn't open?
    open val random = Random(0)

    open val emotes: Emotes = Emotes(EmoteManager.DefaultEmoteManager())
    // TODO: Services (Cinnamon)
    // abstract val services: Services
}