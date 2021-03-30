package net.perfectdreams.loritta.commands.vanilla.`fun`.declarations

import net.perfectdreams.loritta.api.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.api.commands.declarations.required
import net.perfectdreams.loritta.utils.locale.LocaleKeyData

object TextTransformDeclaration {
    object Root : CommandDeclaration(
        name = "texttransform",
        // TODO: Fix Locale
        description = LocaleKeyData("idk")
    ) {
        override val options = Options

        object Options : CommandDeclaration.Options() {
            val quality = subcommand(Quality)
                .register()

            val vaporwave = subcommand(Vaporwave)
                .register()

            val vaporquality = subcommand(VaporQuality)
                .register()
        }
    }

    object Quality : CommandDeclaration(
        name = "quality",
        // TODO: Fix Locale
        description = LocaleKeyData("commands.command.quality.description")
    ) {
        override val options = TextTransformDeclaration.Options
    }

    object Vaporwave : CommandDeclaration(
        name = "vaporwave",
        // TODO: Fix Locale
        description = LocaleKeyData("commands.command.vaporwave.description")
    ) {
        override val options = TextTransformDeclaration.Options
    }

    object VaporQuality : CommandDeclaration(
        name = "vaporquality",
        // TODO: Fix Locale
        description = LocaleKeyData("commands.command.vaporquality.description")
    ) {
        override val options = TextTransformDeclaration.Options
    }

    // Because all of them uses the same "text input" option, we are going to create a single object to keep everything nice and tidy
    object Options : CommandDeclaration.Options() {
        // TODO: Fix Locale
        val text = string("text", LocaleKeyData("idk"))
            .required()
            .register()
    }
}