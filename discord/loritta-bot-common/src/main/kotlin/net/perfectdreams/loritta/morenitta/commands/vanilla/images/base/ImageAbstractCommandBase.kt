package net.perfectdreams.loritta.morenitta.commands.vanilla.images.base

import net.perfectdreams.loritta.morenitta.api.commands.LorittaAbstractCommandBase
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.LorittaBot

/**
 * Extends [LorittaAbstractCommandBase] and sets the [category] to [CommandCategory.IMAGES], simple command images in this plugin
 * should extend this class!
 */
abstract class ImageAbstractCommandBase(loritta: LorittaBot, labels: List<String>) : LorittaAbstractCommandBase(
    loritta,
    labels,
    CommandCategory.IMAGES
)