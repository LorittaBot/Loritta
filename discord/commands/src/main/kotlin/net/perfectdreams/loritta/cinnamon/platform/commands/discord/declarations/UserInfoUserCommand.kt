package net.perfectdreams.loritta.cinnamon.platform.commands.discord.declarations

import io.ktor.client.*
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonUserCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.discord.info.UserInfoUserExecutor

class UserInfoUserCommand(loritta: LorittaCinnamon, val http: HttpClient) : CinnamonUserCommandDeclarationWrapper(loritta) {
    override fun declaration() = userCommand(UserCommand.I18N_PREFIX.Info.ViewUserInfo, UserInfoUserExecutor(loritta, http))
}