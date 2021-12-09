package net.perfectdreams.loritta.cinnamon.platform.commands.social

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.DivorceCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.social.marriage.DivorceProposalButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.social.marriage.ProposalData
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

class DivorceExecutor : CommandExecutor() {
    companion object : CommandExecutorDeclaration(DivorceExecutor::class)

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        val userProfile = context.loritta.services.users.getUserProfile(UserId(context.user.id.value))
            ?: context.failEphemerally {
                styled(
                    context.i18nContext.get(DivorceCommand.I18N_PREFIX.NotMarried),
                    Emotes.Error
                )
            }

        val marriage = userProfile.data.marriage?.let { context.loritta.services.marriages.getMarriage(it) }
            ?: context.failEphemerally {
                styled(
                    context.i18nContext.get(DivorceCommand.I18N_PREFIX.NotMarried),
                    Emotes.Error
                )
            }

        context.sendMessage {
            styled(
                context.i18nContext.get(DivorceCommand.I18N_PREFIX.PrepareToDivorce),
                Emotes.LoriSob
            )

            styled(
                context.i18nContext.get(DivorceCommand.I18N_PREFIX.PleaseConfirm(Emotes.BrokenHeart)),
                Emotes.SmallBlueDiamond
            )

            val partnerId = if (marriage.user1.value == context.user.id.value) {
                marriage.user2.value
            } else {
                marriage.user1.value
            }

            actionRow {
                interactiveButton(
                    ButtonStyle.Primary,
                    DivorceProposalButtonClickExecutor,
                    ComponentDataUtils.encode(
                        ProposalData(
                            context.user.id,
                            Snowflake(partnerId)
                        )
                    )
                ) {
                    emoji = DiscordPartialEmoji(name = Emotes.BrokenHeart.asMention)
                }
            }
        }
    }
}