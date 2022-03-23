package net.perfectdreams.loritta.cinnamon.platform.utils

import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.InteractionContext
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

object LorittaUtils {
    suspend fun checkUserTodayDailyReward(context: InteractionContext): Boolean {
        val dailyState = context.loritta.services.dailies.getUserTodayDailyReward(context.user.id.value.toLong())

        if (dailyState != null) {
            context.sendEphemeralMessage {
                styled(
                    context.i18nContext.get(I18nKeysData.Commands.YouNeedToGetDailyRewardBeforeDoingThisAction),
                    Emotes.Error
                )
            }

            return true
        }
        return false
    }

    suspend fun checkIfUserIsBanned(context: InteractionContext, userId: Snowflake = context.user.id): Boolean {
        val userBannedState = context.loritta.services.users.getUserBannedState(
            UserId(userId.value)
        )

        if (userBannedState != null) {
            val banDateInEpochSeconds = userBannedState.bannedAt.epochSeconds
            val expiresDateInEpochSeconds = userBannedState.expiresAt?.epochSeconds

            if (userBannedState.userId == context.user.id.value.toLong())
                context.sendEphemeralMessage {
                    val banAppealPageUrl = context.loritta.config.website + "extras/faq-loritta/loritta-ban-appeal"
                    content = context.i18nContext.get(
                        if (expiresDateInEpochSeconds != null) {
                            I18nKeysData.Commands.YouAreLorittaBannedTemporary(
                                loriHmpf = Emotes.LoriHmpf,
                                reason = userBannedState.reason,
                                banDate = "<t:$banDateInEpochSeconds:R> (<t:$banDateInEpochSeconds:f>)",
                                expiresDate = "<t:$expiresDateInEpochSeconds:R> (<t:$expiresDateInEpochSeconds:f>)",
                                banAppealPageUrl = banAppealPageUrl,
                                loriAmeno = Emotes.loriAmeno,
                                loriSob = Emotes.LoriSob
                            )
                        } else {
                            I18nKeysData.Commands.YouAreLorittaBannedPermanent(
                                loriHmpf = Emotes.LoriHmpf,
                                reason = userBannedState.reason,
                                banDate = "<t:$banDateInEpochSeconds:R> (<t:$banDateInEpochSeconds:f>)",
                                banAppealPageUrl = banAppealPageUrl,
                                loriAmeno = Emotes.loriAmeno,
                                loriSob = Emotes.LoriSob
                            )
                        }

                    ).joinToString("\n")
                } else {
                context.sendEphemeralMessage {
                    content = context.i18nContext.get(
                        if (expiresDateInEpochSeconds != null) {
                            I18nKeysData.Commands.UserIsLorittaBannedTemporary(
                                userMention = "<@${userId}>",
                                loriHmpf = Emotes.LoriHmpf,
                                reason = userBannedState.reason,
                                banDate = "<t:$banDateInEpochSeconds:R> (<t:$banDateInEpochSeconds:f>)",
                                expiresDate = "<t:$expiresDateInEpochSeconds:R> (<t:$expiresDateInEpochSeconds:f>)"
                            )
                        } else {
                            I18nKeysData.Commands.UserIsLorittaBannedPermanent(
                                userMention = "<@${userId}>",
                                loriHmpf = Emotes.LoriHmpf,
                                reason = userBannedState.reason,
                                banDate = "<t:$banDateInEpochSeconds:R> (<t:$banDateInEpochSeconds:f>)",
                            )
                        }
                    ).joinToString("\n")
                }
            }
            return true
        }
        return false
    }
}