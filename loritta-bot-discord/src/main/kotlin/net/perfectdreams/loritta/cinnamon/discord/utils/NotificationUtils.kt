package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.minn.jda.ktx.messages.InlineMessage
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.serializable.*

object NotificationUtils {
    fun buildUserNotificationMessage(
        loritta: LorittaBot,
        i18nContext: I18nContext,
        notification: UserNotification,
        lorittaWebsiteUrl: String
    ): InlineMessage<*>.() -> Unit {
        when (notification) {
            is DailyTaxTaxedUserNotification -> {
                return UserUtils.buildDailyTaxMessage(
                    loritta,
                    i18nContext,
                    lorittaWebsiteUrl,
                    notification.user,
                    notification
                )
            }
            is DailyTaxWarnUserNotification -> {
                return UserUtils.buildDailyTaxMessage(
                    loritta,
                    i18nContext,
                    lorittaWebsiteUrl,
                    notification.user,
                    notification
                )
            }
            is CorreiosPackageUpdateUserNotification -> {
                return {
                    embed {
                        description = "Notificações de rastreio de pacotes foram removidas"
                    }
                }
            }
            is UnknownUserNotification -> TODO()
        }
    }
}