package net.perfectdreams.loritta.commands.images.base

import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.i18n.I18nKeysData

object SingleImageOptions : CommandOptions() {
    val imageReference = imageReference(
        "image",
        I18nKeysData.Commands.Category.Images.Options.Image
    ).register()
}