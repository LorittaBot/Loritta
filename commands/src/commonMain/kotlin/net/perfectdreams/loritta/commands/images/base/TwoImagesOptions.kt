package net.perfectdreams.loritta.commands.images.base

import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.i18n.I18nKeysData

object TwoImagesOptions : CommandOptions() {
    val imageReference1 = imageReference(
        "image1",
        I18nKeysData.Commands.Category.Images.Options.Image
    ).register()

    val imageReference2 = imageReference(
        "image2",
        I18nKeysData.Commands.Category.Images.Options.Image
    ).register()
}