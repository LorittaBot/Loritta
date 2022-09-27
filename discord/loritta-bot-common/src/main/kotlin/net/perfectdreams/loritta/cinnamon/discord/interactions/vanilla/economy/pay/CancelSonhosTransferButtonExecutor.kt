package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.pay

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.SonhosCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.discord.utils.LoadingEmojis
import net.perfectdreams.loritta.cinnamon.emotes.Emotes

class CancelSonhosTransferButtonExecutor(
    loritta: LorittaBot
) : CinnamonButtonExecutor(loritta) {
    companion object : ButtonExecutorDeclaration(ComponentExecutorIds.CANCEL_SONHOS_TRANSFER_BUTTON_EXECUTOR)

    override suspend fun onClick(user: User, context: ComponentContext) {
        // Only used to check if it is the owner of the transfer
        val decoded = context.decodeDataFromComponentAndRequireUserToMatch<CancelSonhosTransferData>()

        context.updateMessage {
            context.disableComponents(LoadingEmojis.random(), this)
        }

        loritta.pudding.interactionsData.deleteInteractionData(decoded.interactionDataId)

        context.updateMessage {
            actionRow {
                disabledButton(
                    ButtonStyle.Secondary,
                    context.i18nContext.get(SonhosCommand.PAY_I18N_PREFIX.TransferCancelled)
                ) {
                    loriEmoji = Emotes.LoriHmpf
                }
            }
        }
    }
}