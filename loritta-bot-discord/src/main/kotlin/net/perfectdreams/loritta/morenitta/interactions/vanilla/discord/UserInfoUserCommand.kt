package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.IntegrationType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaUserCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.UserCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.UserAndMember
import net.perfectdreams.loritta.morenitta.interactions.commands.userCommand
import java.util.*

class UserInfoUserCommand : UserCommandDeclarationWrapper {
    override fun command() = userCommand(UserCommand.I18N_PREFIX.Info.ViewUserInfo, CommandCategory.DISCORD, UUID.fromString("2ce99a20-4c17-4ad5-bd16-431c02764d4e"), UserInfoUserExecutor()) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
    }

    class UserInfoUserExecutor : LorittaUserCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, user: User) {
            val member = context.guildOrNull?.getMember(user)
            context.reply(true) {
                apply(
                    UserInfoExecutor.createUserInfoMessage(
                        context,
                        UserAndMember(user, member)
                    )
                )
            }
        }
    }
}