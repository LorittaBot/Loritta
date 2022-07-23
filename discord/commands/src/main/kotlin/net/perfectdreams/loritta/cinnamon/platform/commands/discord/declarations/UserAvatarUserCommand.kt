package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.UserCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.avatar.UserAvatarUserExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.userCommand

object UserAvatarUserCommand : UserCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.User

    override fun declaration() = userCommand(I18N_PREFIX.Avatar.ViewAvatar, UserAvatarUserExecutor)
}