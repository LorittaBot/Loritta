package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.base

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.LorittaBot

class TwoImagesOptions(loritta: LorittaBot) : LocalizedApplicationCommandOptions(loritta) {
    val imageReference1 = imageReferenceOrAttachment("image1")

    val imageReference2 = imageReferenceOrAttachment("image2")
}