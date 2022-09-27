package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy

import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.mentionUser
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.SonhosUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.UserId
import net.perfectdreams.loritta.cinnamon.discord.utils.getUserProfile

class SonhosExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val user = optionalUser("user", SonhosCommand.SONHOS_I18N_PREFIX.Options.User)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessage() // Defer because this sometimes takes too long

        val user = args[options.user] ?: context.user

        val profile = context.loritta.pudding.users.getUserProfile(user)
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
                        SonhosCommand.SONHOS_I18N_PREFIX.YouHaveSonhos(
                            SonhosUtils.getSonhosEmojiOfQuantity(userSonhos),
                            userSonhos,
                            if (sonhosRankPosition != null) {
                                SonhosCommand.SONHOS_I18N_PREFIX.YourCurrentRankPosition(
                                    sonhosRankPosition,
                                    loritta.commandMentions.sonhosRank
                                )
                            } else {
                                ""
                            }
                        )
                    ),
                    Emotes.LoriRich
                )
            }

            SonhosUtils.sendEphemeralMessageIfUserHaventGotDailyRewardToday(
                context.loritta,
                context,
                UserId(user.id)
            )
        } else {
            context.sendMessage {
                styled(
                    context.i18nContext.get(
                        SonhosCommand.SONHOS_I18N_PREFIX.UserHasSonhos(
                            mentionUser(user, false), // We don't want to notify the user!
                            SonhosUtils.getSonhosEmojiOfQuantity(userSonhos),
                            userSonhos,
                            if (sonhosRankPosition != null) {
                                SonhosCommand.SONHOS_I18N_PREFIX.UserCurrentRankPosition(
                                    mentionUser(user, false), // Again, we don't want to notify the user!
                                    sonhosRankPosition,
                                    loritta.commandMentions.sonhosRank
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