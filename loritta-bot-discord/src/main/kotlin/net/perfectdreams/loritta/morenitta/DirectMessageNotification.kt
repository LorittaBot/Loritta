package net.perfectdreams.loritta.morenitta

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.utils.NotificationType
import net.perfectdreams.loritta.i18n.I18nKeysData

class DirectMessageNotification(
    val type: NotificationType,
    val title: StringI18nData,
    val description: StringI18nData
) {
    companion object {
        val all = listOf(
            DirectMessageNotification(
                type = NotificationType.DAILY_REMINDER,
                title = I18nKeysData.Commands.Command.Notifications.Configure.Types.DailyReminder.Title,
                description = I18nKeysData.Commands.Command.Notifications.Configure.Types.DailyReminder.Description
            ),
            DirectMessageNotification(
                type = NotificationType.MARRIAGE_EXPIRATION_REMINDER,
                title = I18nKeysData.Commands.Command.Notifications.Configure.Types.MarriageExpirationReminder.Title,
                description = I18nKeysData.Commands.Command.Notifications.Configure.Types.MarriageExpirationReminder.Description
            ),
            DirectMessageNotification(
                type = NotificationType.MARRIAGE_EXPIRED,
                title = I18nKeysData.Commands.Command.Notifications.Configure.Types.MarriageExpired.Title,
                description = I18nKeysData.Commands.Command.Notifications.Configure.Types.MarriageExpired.Description
            ),
            DirectMessageNotification(
                type = NotificationType.MARRIAGE_RENEWED,
                title = I18nKeysData.Commands.Command.Notifications.Configure.Types.MarriageRenewed.Title,
                description = I18nKeysData.Commands.Command.Notifications.Configure.Types.MarriageRenewed.Description
            ),
            DirectMessageNotification(
                type = NotificationType.MARRIAGE_LOVE_LETTER,
                title = I18nKeysData.Commands.Command.Notifications.Configure.Types.MarriageLoveLetter.Title,
                description = I18nKeysData.Commands.Command.Notifications.Configure.Types.MarriageLoveLetter.Description
            ),
            DirectMessageNotification(
                type = NotificationType.EXPERIENCE_LEVEL_UP,
                title = I18nKeysData.Commands.Command.Notifications.Configure.Types.ExperienceLevelUp.Title,
                description = I18nKeysData.Commands.Command.Notifications.Configure.Types.ExperienceLevelUp.Description
            ),
            DirectMessageNotification(
                type = NotificationType.GIVEAWAY_ENDED,
                title = I18nKeysData.Commands.Command.Notifications.Configure.Types.ManagedGiveawayEnded.Title,
                description = I18nKeysData.Commands.Command.Notifications.Configure.Types.ManagedGiveawayEnded.Description
            )
        )
    }
}