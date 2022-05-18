package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.UserCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.ViewAvatarExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.userCommand

object ViewAvatarCommand : UserCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.User

    override fun declaration() = userCommand("Ver Avatar", ViewAvatarExecutor)
}