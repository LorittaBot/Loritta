package net.perfectdreams.loritta.morenitta.interactions.vanilla.discord

import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaUserCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.UserCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.UserAndMember
import net.perfectdreams.loritta.morenitta.interactions.commands.userCommand

class UserAvatarUserCommand : UserCommandDeclarationWrapper {
    override fun command() = userCommand(UserCommand.I18N_PREFIX.Avatar.ViewAvatar, CommandCategory.DISCORD, UserAvatarUserExecutor())

    class UserAvatarUserExecutor : LorittaUserCommandExecutor() {
        override suspend fun execute(context: ApplicationCommandContext, user: User) {
            val member = context.guildOrNull?.getMember(user)
            context.reply(true) {
                apply(
                    UserCommand.createAvatarMessage(
                        CommandContextCompat.InteractionsCommandContextCompat(context),
                        UserAndMember(user, member),
                        UserCommand.Companion.AvatarTarget.GLOBAL_AVATAR
                    )
                )
            }
        }
    }
}