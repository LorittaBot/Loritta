package net.perfectdreams.loritta.morenitta.interactions.vanilla.economy

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaUserCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.UserCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.userCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.SonhosAtmExecutor.InformationType
import java.util.*

class SonhosAtmUserCommand(val loritta: LorittaBot) : UserCommandDeclarationWrapper {
    override fun command() = userCommand(I18nKeysData.Commands.Command.Sonhosatm.ViewUserSonhos, CommandCategory.ECONOMY, UUID.fromString("224be6ab-ed68-466f-a364-af63d3580310"), SonhosAtmUserExecutor(loritta)) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
    }

    class SonhosAtmUserExecutor(val loritta: LorittaBot) : LorittaUserCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, user: User) {
            SonhosAtmExecutor.executeSonhosAtm(
                loritta,
                context,
                true,
                user,
                InformationType.NORMAL
            )
        }
    }
}