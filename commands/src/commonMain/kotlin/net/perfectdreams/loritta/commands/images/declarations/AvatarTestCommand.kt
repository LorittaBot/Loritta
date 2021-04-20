package net.perfectdreams.loritta.commands.images.declarations

import net.perfectdreams.loritta.commands.images.AvatarTestExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.commands.declarations.CommandDeclaration
import net.perfectdreams.loritta.common.commands.declarations.command
import net.perfectdreams.loritta.common.locale.LocaleKeyData

object AvatarTestCommand : CommandDeclaration {
    const val LOCALE_PREFIX = "commands.command.avatartest"

    override fun declaration() = command(listOf("avatartest"), CommandCategory.IMAGES) {
        description = LocaleKeyData("${LOCALE_PREFIX}.description")
        executor = AvatarTestExecutor
    }
}