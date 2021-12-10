package net.perfectdreams.loritta.cinnamon.platform.commands.social

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.RepCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.styled

class RepExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(RepExecutor::class) {
        object Options : CommandOptions() {
            val user = user("user", RepCommand.I18N_PREFIX.Options.User)
                .register()
        }

        override val options = Options
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        val user = args[Options.user]

        if (user.id == context.user.id)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(RepCommand.I18N_PREFIX.RepSelf),
                    Emotes.Error
                )
            }

        context.loritta.services.dailies.getUserTodayDailyReward(context.user.id.value.toLong())
            ?: context.failEphemerally {
                styled(
                    context.i18nContext.get(I18nKeysData.Commands.YouNeedToGetDailyRewardBeforeDoingThisAction),
                    Emotes.Error
                )
            }

        val lastReputationGiven = context.loritta.services.reputations.getLastReputationGiven(context.user.id.value.toLong())

        if (lastReputationGiven != null) {
            val diff = System.currentTimeMillis() - lastReputationGiven.receivedAt

            if (3_600_000 > diff)
                context.failEphemerally {
                    styled(
                        context.i18nContext.get(
                            RepCommand.I18N_PREFIX.Wait(
                                "<t:${(lastReputationGiven.receivedAt + 3.6e+6) / 1000}:R>"
                            )
                        ),
                        Emotes.Error
                    )
                }
        }

        var url = "${context.loritta.config.website}user/${user.id.value}/rep"

        if (context is GuildApplicationCommandContext)
            url += "?guild=${context.guildId.value}&channel=${context.interaKTionsContext.channelId.value}"

        context.sendMessage {
            styled(
                context.i18nContext.get(RepCommand.I18N_PREFIX.ReputationLink(url)),
                Emotes.LoriHappy
            )
        }
    }
}