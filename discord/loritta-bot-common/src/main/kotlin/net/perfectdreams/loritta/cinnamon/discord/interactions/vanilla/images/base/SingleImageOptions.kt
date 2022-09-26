package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.base

import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions

class SingleImageOptions(loritta: LorittaCinnamon) : LocalizedApplicationCommandOptions(loritta) {
    val imageReference = imageReferenceOrAttachment("image")
}