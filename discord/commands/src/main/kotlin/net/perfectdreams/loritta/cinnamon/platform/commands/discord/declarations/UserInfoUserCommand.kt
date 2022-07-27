package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonUserCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.info.UserInfoUserExecutor

class UserInfoUserCommand(languageManager: LanguageManager) : CinnamonUserCommandDeclarationWrapper(languageManager) {
    override fun declaration() = userCommand(UserCommand.I18N_PREFIX.Info.ViewUserInfo, { UserInfoUserExecutor(it, it.http) })
}