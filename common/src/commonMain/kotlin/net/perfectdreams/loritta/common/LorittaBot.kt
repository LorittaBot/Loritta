package net.perfectdreams.loritta.common

import net.perfectdreams.loritta.common.emotes.EmoteManager
import net.perfectdreams.loritta.common.emotes.Emotes
import kotlin.random.Random

/**
 * Represents a Loritta Morenitta implementation.
 *
 * This should be extended by plataform specific Lori's :3
 */
abstract class LorittaBot {
    // TODO: *Really* set a random seed
    // TODO: Maybe it would be better if this wasn't open?
    open val random = Random(0)

    open val emotes: Emotes = Emotes(EmoteManager.DefaultEmoteManager())
    // TODO: Services (Cinnamon)
    // abstract val services: Services
}