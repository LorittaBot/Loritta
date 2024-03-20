package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.Command
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaUserCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.UserCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.UserAndMember
import net.perfectdreams.loritta.morenitta.interactions.commands.userCommand

class UserAvatarUserCommand : UserCommandDeclarationWrapper {
    override fun command() = userCommand(UserCommand.I18N_PREFIX.Avatar.ViewAvatar, CommandCategory.DISCORD, UserAvatarUserExecutor()) {
        this.integrationTypes = listOf(Command.IntegrationType.GUILD_INSTALL, Command.IntegrationType.USER_INSTALL)
    }

    class UserAvatarUserExecutor : LorittaUserCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, user: User) {
            val member = context.guildOrNull?.getMember(user)
            context.reply(true) {
                apply(
                    UserAvatarExecutor.createAvatarMessage(
                        context,
                        UserAndMember(user, member),
                        UserAvatarExecutor.AvatarTarget.GLOBAL_AVATAR
                    )
                )
            }
        }
    }
}