package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonUserCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.avatar.UserAvatarUserExecutor

class UserAvatarUserCommand(languageManager: LanguageManager) : CinnamonUserCommandDeclarationWrapper(languageManager) {
    val I18N_PREFIX = I18nKeysData.Commands.Command.User

    override fun declaration() = userCommand(I18N_PREFIX.Avatar.ViewAvatar, { UserAvatarUserExecutor(it) })
}