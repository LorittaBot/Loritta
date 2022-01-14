package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.utils.getUserProfile

class SonhosExecutor() : CommandExecutor() {
    companion object : CommandExecutorDeclaration(SonhosExecutor::class) {
        object Options : CommandOptions() {
            val user = optionalUser("user", SonhosCommand.I18N_PREFIX.Options.User)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        context.deferChannelMessage() // Defer because this sometimes takes too long

        val user = args[options.user] ?: context.user

        val profile = context.loritta.services.users.getUserProfile(user)
        val userSonhos = profile?.money ?: 0L
        val isSelf = context.user.id == user.id

        // Needs to be in here because MessageBuilder is not suspendable!
        val sonhosRankPosition = if (userSonhos != 0L && profile != null) // Only show the ranking position if the user has any sonhos, this avoids querying the db with useless stuff
            profile.getRankPositionInSonhosRanking()
        else
            null

        context.sendMessage {
            if (isSelf) {
                styled(
                    context.i18nContext.get(
                        SonhosCommand.I18N_PREFIX.YouHaveSonhos(
                            userSonhos,
                            if (sonhosRankPosition != null) {
                                SonhosCommand.I18N_PREFIX.YourCurrentRankPosition(
                                    sonhosRankPosition,
                                    "+sonhos top" // TODO: Change to slash command
                                )
                            } else {
                                ""
                            }
                        )
                    ),
                    Emotes.LoriRich
                )
            } else {
                styled(
                    context.i18nContext.get(
                        SonhosCommand.I18N_PREFIX.UserHasSonhos(
                            mentionUser(user, false), // We don't want to notify the user!
                            userSonhos,
                            if (sonhosRankPosition != null) {
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
                    Emotes.LoriRich
                )
            }
        }
    }
}