package net.perfectdreams.loritta.commands.images.base

import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object SingleImageOptions : CommandOptions() {
    val imageReference = imageReference(
        "image",
        LocaleKeyData("TODO_FIX_THIS")
    ).register()
}