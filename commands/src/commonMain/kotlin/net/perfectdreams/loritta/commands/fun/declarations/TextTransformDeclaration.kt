package net.perfectdreams.loritta.commands.`fun`.declarations

import net.perfectdreams.loritta.commands.`fun`.TextQualityExecutor
import net.perfectdreams.loritta.commands.`fun`.TextVaporQualityExecutor
import net.perfectdreams.loritta.commands.`fun`.TextVaporwaveExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object TextTransformDeclaration : CommandDeclaration {
    override fun declaration() = command(listOf("text", "texto")) {
        description = LocaleKeyData("TODO_FIX_THIS")

        subcommand(listOf("vaporwave", "vaporonda")) {
            description = LocaleKeyData("commands.command.vaporwave.description")
            executor = TextVaporwaveExecutor
        }

        subcommand(listOf("quality", "qualidade")) {
            description = LocaleKeyData("commands.command.quality.description")
            executor = TextQualityExecutor
        }

        subcommand(listOf("vaporquality", "vaporqualidade")) {
            description = LocaleKeyData("commands.command.vaporquality.description")
            executor = TextVaporQualityExecutor
        }
    }
}