package net.perfectdreams.loritta.cinnamon.platform.commands.social

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.builder.message.actionRow
import net.perfectdreams.discordinteraktions.common.builder.message.allowedMentions
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.InteractionContext
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.social.declarations.MarryCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.social.marriage.MarriageProposalButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.social.marriage.ProposalData
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.components.interactiveButton
import net.perfectdreams.loritta.cinnamon.platform.utils.ComponentDataUtils
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingUserProfile

class MarryExecutor() : CommandExecutor() {
    companion object : CommandExecutorDeclaration(MarryExecutor::class) {
        object Options : CommandOptions() {
            val user = user("user", MarryCommand.I18N_PREFIX.Options.User)
                .register()
        }

        override val options = Options

        const val MARRIAGE_COST = 7_500L

        suspend fun canMarried(
            context: InteractionContext,
            senderId: Snowflake,
            proposedUserId: Snowflake
        ): Pair<PuddingUserProfile, PuddingUserProfile> {
            val userProfile = context.loritta.services.users.getUserProfile(UserId(senderId.value))
            val proposeToProfile = context.loritta.services.users.getUserProfile(
                UserId(proposedUserId.value)
            )

            if (userProfile == null || MARRIAGE_COST > userProfile.money)
                context.failEphemerally {
                    styled(
                        context.i18nContext.get(MarryCommand.I18N_PREFIX.InsufficientFunds),
                        Emotes.Error
                    )
                }

            if (proposeToProfile == null || MARRIAGE_COST > proposeToProfile.money)
                context.failEphemerally {
                    styled(
                        context.i18nContext.get(
                            MarryCommand.I18N_PREFIX.InsufficientFundsOther("<@${proposedUserId.value}>")
                        ),
                        Emotes.Error
                    )
                }

            val senderMarriage = userProfile.getMarriageOrNull()
            val proposedUserMarriage = proposeToProfile.getMarriageOrNull()

            if (senderMarriage != null)
                context.failEphemerally {
                    styled(
                        context.i18nContext.get(MarryCommand.I18N_PREFIX.AlreadyMarried),
                        Emotes.Error
                    )
                }

            if (proposedUserMarriage != null)
                context.failEphemerally {
                    styled(
                        "<@${proposedUserId.value}> " + context.i18nContext.get(MarryCommand.I18N_PREFIX.AlreadyMarriedOther),
                        Emotes.Error
                    )
                }

            return Pair(userProfile, proposeToProfile)
        }
    }

    override suspend fun execute(context: ApplicationCommandContext, args: CommandArguments) {
        if (context !is GuildApplicationCommandContext)
            context.fail {
                content = context.i18nContext.get(I18nKeysData.Commands.CommandOnlyAvailableInGuilds)
            }

        val proposedUser = args[Options.user]

        if (proposedUser.id == context.user.id)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(MarryCommand.I18N_PREFIX.CantMarryYourself),
                    Emotes.Error
                )
            }

        if (proposedUser.id.value.toLong() == context.loritta.discordConfig.applicationId)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(MarryCommand.I18N_PREFIX.MarryLoritta),
                    Emotes.SmolLoriPutassa
                )
            }

        if (proposedUser.bot)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(MarryCommand.I18N_PREFIX.MarryBot),
                    Emotes.LoriSob
                )
            }

        context.deferChannelMessage()

        canMarried(context, context.user.id, proposedUser.id)

        context.sendMessage {
            allowedMentions {
                users.addAll(listOf(proposedUser.id, context.user.id))
            }

            styled(
                "<@${proposedUser.id.value}> "
                        + context.i18nContext.get(MarryCommand.I18N_PREFIX.PrepareToMarry)
                        + " <@${context.user.id.value}>",
                Emotes.MarriageRing
            )

            styled(
                context.i18nContext.get(MarryCommand.I18N_PREFIX.MarriageProposal),
                Emotes.Dollar
            )

            actionRow {
                interactiveButton(
                    ButtonStyle.Primary,
                    MarriageProposalButtonClickExecutor,
                    ComponentDataUtils.encode(
                        ProposalData(proposedUser.id, context.user.id)
                    )
                ) {
                    emoji = DiscordPartialEmoji(name = Emotes.MarriageRing.asMention)
                }
            }
        }
    }
}