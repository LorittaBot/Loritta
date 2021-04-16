package net.perfectdreams.loritta.commands.images.base

import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object TwoImagesOptions : CommandOptions() {
    val imageReference1 = imageReference(
        "image1",
        LocaleKeyData("TODO_FIX_THIS")
    ).register()

    val imageReference2 = imageReference(
        "image2",
        LocaleKeyData("TODO_FIX_THIS")
    ).register()
}