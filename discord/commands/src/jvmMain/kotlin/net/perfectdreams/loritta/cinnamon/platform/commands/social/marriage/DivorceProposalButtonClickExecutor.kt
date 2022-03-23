package net.perfectdreams.loritta.cinnamon.platform.commands.social.marriage

import dev.kord.common.Color
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.json.request.DMCreateRequest
import dev.kord.rest.request.RestRequestException
import dev.kord.rest.service.RestClient
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.DivorceCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.components.*
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class DivorceProposalButtonClickExecutor(val rest: RestClient) : ButtonClickWithDataExecutor {
    companion object : ButtonClickExecutorDeclaration(
        DivorceProposalButtonClickExecutor::class,
        ComponentExecutorIds.DIVORCE_PROPOSAL_BUTTON_EXECUTOR
    )

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        val (authorId, marriagePartnerID) = context.decodeViaComponentDataUtilsAndRequireUserToMatch<ProposalData>(data)

        context.updateMessage {
            components = mutableListOf()

            actionRow {
                interactionButton(
                    ButtonStyle.Primary,
                    "####"
                ) {
                    emoji = DiscordPartialEmoji(name = Emotes.BrokenHeart.asMention)
                    disabled = true
                }
            }
        }

        val authorProfile = context.loritta.services.users.getUserProfile(UserId(authorId.value))

        if (authorProfile?.data?.marriage == null)
            context.fail {
                styled(
                    context.i18nContext.get(DivorceCommand.I18N_PREFIX.NotMarried),
                    Emotes.Error
                )
            }

        context.loritta.services.marriages.marriageDivorceAndDelete(authorProfile.data.marriage!!)

        val userDM = rest.user.createDM(DMCreateRequest(marriagePartnerID))

        try {
            rest.channel.createMessage(userDM.id) {
                embed {
                    title = context.i18nContext.get(DivorceCommand.I18N_PREFIX.DivorcedTitle)
                    description = context.i18nContext.get(DivorceCommand.I18N_PREFIX.DivorcedDescription)

                    color = Color(114, 137, 218) // TODO: Move this to an object

                    thumbnailUrl = Emotes.LoriSob.asUrl
                }
            }
        } catch (_: RestRequestException) { }

        context.sendMessage {
            styled(
                context.i18nContext.get(DivorceCommand.I18N_PREFIX.Divorced(Emotes.LoriHeart)),
                Emotes.Error
            )
        }
    }
}