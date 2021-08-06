package net.perfectdreams.loritta.platform.discord.utils.declarations

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.toI18nHelper
import net.perfectdreams.loritta.platform.discord.utils.AvatarExecutor

object AvatarCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.avatar"

    override fun declaration() = command(listOf("avatar"), CommandCategory.DISCORD, LocaleKeyData("$LOCALE_PREFIX.description").toI18nHelper()) {
        executor = AvatarExecutor
    }
}