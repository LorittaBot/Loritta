package net.perfectdreams.loritta.cinnamon.platform.commands.images.base

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions

object SingleImageOptions : ApplicationCommandOptions() {
    val imageReference = imageReference(
        "image",
        I18nKeysData.Commands.Category.Images.Options.Image
    ).register()
}