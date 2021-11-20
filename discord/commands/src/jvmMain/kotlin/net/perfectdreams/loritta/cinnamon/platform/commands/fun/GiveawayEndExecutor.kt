package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`

import dev.kord.rest.service.RestClient
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.GiveawayCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.utils.giveaway.GiveawayManager

class GiveawayEndExecutor(private val rest: RestClient) : CommandExecutor() {
    companion object : CommandExecutorDeclaration(GiveawayEndExecutor::class) {
        object Options : CommandOptions() {
            val messageLink = string("message_link", GiveawayCommand.I18N_PREFIX.End.Options.MessageLink)
                .register()
        }

        override val options = Options

        val numberRegex = Regex("[0-9]+")
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        val messageId = args[Options.messageLink].split("/").last()
        if (numberRegex.find(messageId) == null)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(GiveawayCommand.I18N_PREFIX.Reroll.InvalidLink),
                    Emotes.Error
                )
            }

        val giveaway = context.loritta.services.giveaways.getGiveawayOrNullByMessageId(messageId.toLong())
            ?: context.failEphemerally {
                styled(
                    context.i18nContext.get(GiveawayCommand.I18N_PREFIX.Reroll.InvalidGiveaway),
                    Emotes.LoriSob
                )
            }

        if (giveaway.finished)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(GiveawayCommand.I18N_PREFIX.End.AlreadyFinished),
                    Emotes.Error
                )
            }

        GiveawayManager.finishGiveaway(giveaway, rest, context.i18nContext, giveaway.numberOfWinners)

        context.sendEphemeralMessage {
            styled(
                context.i18nContext.get(GiveawayCommand.I18N_PREFIX.End.FinishedGiveaway),
                Emotes.Tada
            )
        }
    }
}