package net.perfectdreams.loritta.commands.economy

import net.perfectdreams.loritta.commands.`fun`.declarations.CancelledCommand
import net.perfectdreams.loritta.commands.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.common.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.commands.CommandExecutor
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.commands.options.CommandOptions
import net.perfectdreams.loritta.common.emotes.Emotes

class SonhosExecutor(val emotes: Emotes) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(SonhosExecutor::class) {
        object Options : CommandOptions() {
            val user = optionalUser("user", CancelledCommand.I18N_PREFIX.Options.User)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        val user = args[options.user] ?: context.user

        val profile = context.loritta.services.users.getUserProfileById(user.id)
        val userSonhos = profile?.money ?: 0L
        val isSelf = context.user.id == user.id

        // Needs to be in here because MessageBuilder is not suspendable!
        val sonhosRankPosition = if (userSonhos != 0L) // Only show the ranking position if the user has any sonhos, this avoids querying the db with useless stuff
            context.loritta.services.sonhos.getSonhosRankPositionBySonhos(userSonhos)
        else
            0L

        context.sendMessage {
            if (isSelf) {
                styled(
                    context.i18nContext.get(
                        SonhosCommand.I18N_PREFIX.YouHaveSonhos(
                            userSonhos,
                            if (sonhosRankPosition != 0L) {
                                SonhosCommand.I18N_PREFIX.YourCurrentRankPosition(
                                    sonhosRankPosition,
                                    "+sonhos top" // TODO: Change to slash command
                                )
                            } else {
                                ""
                            }
                        )
                    ),
                    emotes.loriRich
                )
            } else {
                styled(
                    context.i18nContext.get(
                        SonhosCommand.I18N_PREFIX.UserHasSonhos(
                            mentionUser(user, false), // We don't want to notify the user!
                            userSonhos,
                            if (sonhosRankPosition != 0L) {
                                SonhosCommand.I18N_PREFIX.UserCurrentRankPosition(
                                    mentionUser(user, false), // Again, we don't want to notify the user!
                                    sonhosRankPosition,
                                    "+sonhos top" // TODO: Change to slash command
                                )
                            } else {
                                ""
                            }
                        )
                    ),
                    emotes.loriRich
                )
            }
        }
    }
}