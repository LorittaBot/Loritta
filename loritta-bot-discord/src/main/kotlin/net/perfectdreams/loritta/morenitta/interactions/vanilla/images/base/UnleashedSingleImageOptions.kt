package net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base

import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions

class UnleashedSingleImageOptions : ApplicationCommandOptions() {
    val imageReference = imageReferenceOrAttachment("image", TodoFixThisData)
}