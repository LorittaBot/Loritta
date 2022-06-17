package net.perfectdreams.loritta.cinnamon.platform.commands.economy

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.platform.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.utils.SonhosUtils.userHaventGotDailyTodayOrUpsellSonhosBundles
import net.perfectdreams.loritta.cinnamon.platform.utils.UserId
import net.perfectdreams.loritta.cinnamon.platform.utils.getUserProfile

class SonhosExecutor() : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration() {
        object Options : ApplicationCommandOptions() {
            val user = optionalUser("user", SonhosCommand.I18N_PREFIX.Options.User)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
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

        if (isSelf) {
            context.sendMessage {
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
            }

            context.sendEphemeralMessage {
                userHaventGotDailyTodayOrUpsellSonhosBundles(
                    context.loritta,
                    context.i18nContext,
                    UserId(user.id),
                    "sonhos",
                    "viewing-own-sonhos"
                )
            }
        } else {
            context.sendMessage {
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