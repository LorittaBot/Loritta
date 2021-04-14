package net.perfectdreams.loritta.common

import net.perfectdreams.loritta.common.builder.BuilderFactory

/**
 * Represents a Loritta Morenitta implementation.
 *
 * This should be extended by plataform specific Lori's :3
 */
abstract class LorittaBot {
    open val builderFactory: BuilderFactory = BuilderFactory.DefaultBuilderFactory
}