package net.perfectdreams.loritta.common

import kotlin.random.Random

/**
 * Represents a Loritta Morenitta implementation.
 *
 * This should be extended by plataform specific Lori's :3
 */
abstract class LorittaBot {
    // TODO: *Really* set a random seed
    val random = Random(0)
}