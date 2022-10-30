package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.base

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.LorittaBot

class SingleImageOptions(loritta: LorittaBot) : LocalizedApplicationCommandOptions(loritta) {
    val imageReference = imageReferenceOrAttachment("image")
}