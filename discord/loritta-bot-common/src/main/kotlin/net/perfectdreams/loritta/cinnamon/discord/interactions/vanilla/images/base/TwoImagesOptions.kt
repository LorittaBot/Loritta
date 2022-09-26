package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.base

import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions

class TwoImagesOptions(loritta: LorittaCinnamon) : LocalizedApplicationCommandOptions(loritta) {
    val imageReference1 = imageReferenceOrAttachment("image1")

    val imageReference2 = imageReferenceOrAttachment("image2")
}