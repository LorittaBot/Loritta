package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.NichijouYuukoPaperExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper

object NichijouYuukoPaperCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.discordping"

    override fun declaration() = command(listOf("discordping", "discórdia", "discordia"), CommandCategory.IMAGES, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = NichijouYuukoPaperExecutor
    }
}