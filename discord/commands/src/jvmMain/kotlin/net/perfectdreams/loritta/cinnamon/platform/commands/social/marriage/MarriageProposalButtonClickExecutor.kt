package net.perfectdreams.loritta.cinnamon.platform.commands.social.marriage

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import net.perfectdreams.discordinteraktions.api.entities.User
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds.MARRIAGE_PROPOSAL_BUTTON_EXECUTOR
import net.perfectdreams.loritta.cinnamon.platform.commands.social.MarryExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.MarryCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.buttons.ButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.buttons.ButtonClickExecutorDeclaration

class MarriageProposalButtonClickExecutor : ButtonClickExecutor {
    companion object : ButtonClickExecutorDeclaration(
        MarriageProposalButtonClickExecutor::class,
        MARRIAGE_PROPOSAL_BUTTON_EXECUTOR
    )

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        val (proposedUserId, authorId) = context.decodeViaComponentDataUtilsAndRequireUserToMatch<ProposalData>(data)

        context.updateMessage {
            components = mutableListOf()

            actionRow {
                interactionButton(
                    ButtonStyle.Primary,
                    "####"
                ) {
                    emoji = DiscordPartialEmoji(name = Emotes.MarriageRing.asMention)
                    disabled = true
                }
            }
        }

        val (userProfile, proposeToProfile) = MarryExecutor.canMarried(context, authorId, proposedUserId)

        context.loritta.services.marriages.marry(userProfile, proposeToProfile, MarryExecutor.MARRIAGE_COST)

        context.sendMessage {
            styled(
                context.i18nContext.get(MarryCommand.I18N_PREFIX.SucessfulMarriage),
                Emotes.LoriHeart
            )
        }
    }
}