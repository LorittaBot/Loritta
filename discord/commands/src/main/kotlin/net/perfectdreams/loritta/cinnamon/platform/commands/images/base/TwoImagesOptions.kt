package net.perfectdreams.loritta.cinnamon.platform.commands.images.base

import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions

class TwoImagesOptions(loritta: LorittaCinnamon) : LocalizedApplicationCommandOptions(loritta) {
    val imageReference1 = imageReferenceOrAttachment("image1")

    val imageReference2 = imageReferenceOrAttachment("image2")
}