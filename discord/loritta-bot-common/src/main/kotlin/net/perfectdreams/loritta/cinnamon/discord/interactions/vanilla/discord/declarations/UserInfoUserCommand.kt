package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonUserCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.info.UserInfoUserExecutor
import net.perfectdreams.loritta.common.locale.LanguageManager

class UserInfoUserCommand(languageManager: LanguageManager) : CinnamonUserCommandDeclarationWrapper(languageManager) {
    override fun declaration() =
        userCommand(UserCommand.I18N_PREFIX.Info.ViewUserInfo, { UserInfoUserExecutor(it, it.http) })
}