package net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.giveaway

import net.perfectdreams.discordinteraktions.api.entities.User
import net.perfectdreams.discordinteraktions.common.context.components.GuildComponentContext
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ComponentExecutorIds
import net.perfectdreams.loritta.cinnamon.platform.commands.`fun`.declarations.GiveawayCommand
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.components.ComponentContext
import net.perfectdreams.loritta.cinnamon.platform.components.buttons.ButtonClickExecutor
import net.perfectdreams.loritta.cinnamon.platform.components.buttons.ButtonClickExecutorDeclaration

class JoinButtonClickExecutor : ButtonClickExecutor {
    companion object : ButtonClickExecutorDeclaration(
        JoinButtonClickExecutor::class,
        ComponentExecutorIds.GIVEAWAY_JOIN_BUTTON_EXECUTOR
    )

    override suspend fun onClick(user: User, context: ComponentContext, data: String) {
        if (user.id.timestamp.toEpochMilliseconds() <= 604800000)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(GiveawayCommand.I18N_PREFIX.Button.SelfAccountIsTooNew),
                    Emotes.Error
                )
            }

        val (rolesNotToParticipate) = context.decodeViaComponentDataUtils<JoinButtonData>(data)

        val messageId = context.interaKTionsContext.message.id.value.toLong()
        val userId = user.id.asString
        val member = (context.interaKTionsContext as GuildComponentContext).member
        val containsRole = rolesNotToParticipate?.filter { it in member.roles }?.ifEmpty { null }

        if (containsRole != null)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(
                        GiveawayCommand.I18N_PREFIX.Button.MemberRoleNotAllowed(
                            containsRole.joinToString { "<@&${it.value}>" }
                        )
                    ),
                    Emotes.Error
                )
            }

        val giveaway = context.loritta.services.giveaways.getGiveawayOrNullByMessageId(
            messageId
        )

        if (giveaway == null || giveaway.finished)
            context.failEphemerally {
                styled(
                    context.i18nContext.get(GiveawayCommand.I18N_PREFIX.Button.GiveawayAlreadyFinished),
                    Emotes.LoriSob
                )
            }

        if (userId in giveaway.users) {
            giveaway.removeUserFromGiveaway(userId)

            context.sendEphemeralMessage {
                styled(
                    context.i18nContext.get(
                        GiveawayCommand.I18N_PREFIX.Button.RemovedUserFromGiveaway
                    ),
                    Emotes.Tada
                )
            }
        } else {
            giveaway.addUserInGiveaway(userId)

            context.sendEphemeralMessage {
                styled(
                    context.i18nContext.get(
                        GiveawayCommand.I18N_PREFIX.Button.SuccessfullyAddedUserInGiveaway(giveaway.users.size + 1)
                    ),
                    Emotes.Tada
                )
            }
        }
    }
}