package net.perfectdreams.loritta.cinnamon.platform.commands.utils.notifications.notificationtransformers

import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.pudding.data.UserNotification

interface NotificationTransformer<T : UserNotification> {
    /**
     * Creates a [StringBuilder] block that appends the [notification] into a [StringBuilder].
     */
    suspend fun transform(
        loritta: LorittaCinnamon,
        i18nContext: I18nContext,
        notification: T
    ): suspend StringBuilder.() -> (Unit)

    fun StringBuilder.appendMoneyLostEmoji() {
        append(Emotes.MoneyWithWings)
        append(" ")
    }

    fun StringBuilder.appendMoneyEarnedEmoji() {
        append(Emotes.DollarBill)
        append(" ")
    }
}