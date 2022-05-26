package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.platform.commands.UserCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.info.UserInfoUserExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.userCommand

object UserInfoUserCommand : UserCommandDeclarationWrapper {
    override fun declaration() = userCommand(UserCommand.I18N_PREFIX.Info.ViewUserInfo, UserInfoUserExecutor)
}