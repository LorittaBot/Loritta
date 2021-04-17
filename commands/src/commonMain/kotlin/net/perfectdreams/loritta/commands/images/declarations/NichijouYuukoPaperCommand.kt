package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.NichijouYuukoPaperExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object NichijouYuukoPaperCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.discordping"

    override fun declaration() = command(listOf("discordping", "discórdia", "discordia")) {
        description = LocaleKeyData("$LOCALE_PREFIX.description")
        executor = NichijouYuukoPaperExecutor
    }
}