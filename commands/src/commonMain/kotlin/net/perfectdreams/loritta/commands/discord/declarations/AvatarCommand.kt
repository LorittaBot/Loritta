package net.perfectdreams.loritta.commands.discord.declarations

import net.perfectdreams.loritta.commands.`fun`.CancelledExecutor
import net.perfectdreams.loritta.commands.discord.AvatarExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object AvatarCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.avatar"

    override fun declaration() = command(listOf("avatar"), CommandCategory.DISCORD) {
        description = LocaleKeyData("${LOCALE_PREFIX}.description")
        executor = AvatarExecutor
    }
}