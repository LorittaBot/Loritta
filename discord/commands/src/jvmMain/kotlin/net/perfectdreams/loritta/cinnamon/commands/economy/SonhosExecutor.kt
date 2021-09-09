package net.perfectdreams.loritta.cinnamon.commands.economy

import net.perfectdreams.loritta.cinnamon.commands.`fun`.declarations.CancelledCommand
import net.perfectdreams.loritta.cinnamon.commands.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandContext
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.discord.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.discord.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.discord.commands.styled

class SonhosExecutor() : CommandExecutor() {
    companion object : CommandExecutorDeclaration(SonhosExecutor::class) {
        object Options : CommandOptions() {
            val user = optionalUser("user", CancelledCommand.I18N_PREFIX.Options.User)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: CommandContext, args: CommandArguments) {
        context.deferChannelMessage() // Defer because this sometimes takes too long

        val user = args[options.user] ?: context.user

        val profile = context.loritta.services.users.getUserProfileById(user.id.value)
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
                    Emotes.loriRich
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
                    Emotes.loriRich
                )
            }
        }
    }
}