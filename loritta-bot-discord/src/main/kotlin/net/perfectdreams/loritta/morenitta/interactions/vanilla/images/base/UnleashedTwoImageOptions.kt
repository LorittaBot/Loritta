package net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base

import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions

class UnleashedTwoImageOptions : ApplicationCommandOptions() {
    val imageReference1 = imageReferenceOrAttachment("image1", TodoFixThisData)
    val imageReference2 = imageReferenceOrAttachment("image2", TodoFixThisData)
}