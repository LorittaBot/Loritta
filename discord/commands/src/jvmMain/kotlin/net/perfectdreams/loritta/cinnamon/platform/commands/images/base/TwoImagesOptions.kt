package net.perfectdreams.loritta.cinnamon.platform.commands.images.base

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions

object TwoImagesOptions : ApplicationCommandOptions() {
    val imageReference1 = imageReferenceOrAttachment(
        "image1",
        I18nKeysData.Commands.Category.Images.Options.Image
    ).register()

    val imageReference2 = imageReferenceOrAttachment(
        "image2",
        I18nKeysData.Commands.Category.Images.Options.Image
    ).register()
}