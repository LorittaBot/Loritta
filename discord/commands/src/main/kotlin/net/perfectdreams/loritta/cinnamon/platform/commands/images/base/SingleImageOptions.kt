package net.perfectdreams.loritta.cinnamon.platform.commands.images.base

import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions

class SingleImageOptions(loritta: LorittaCinnamon) : LocalizedApplicationCommandOptions(loritta) {
    val imageReference = imageReferenceOrAttachment("image")
}