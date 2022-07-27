package net.perfectdreams.loritta.cinnamon.platform.commands.utils.notifications

import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaColors
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.NotificationsCommand
import net.perfectdreams.loritta.cinnamon.platform.utils.UserId
import net.perfectdreams.loritta.cinnamon.platform.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.pudding.data.CorreiosPackageUpdateUserNotification
import net.perfectdreams.loritta.cinnamon.pudding.data.DailyTaxTaxedUserNotification
import net.perfectdreams.loritta.cinnamon.pudding.data.DailyTaxWarnUserNotification
import net.perfectdreams.loritta.cinnamon.pudding.data.UnknownUserNotification

class NotificationsListExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {


    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally()

        val notifications = context.loritta.services.notifications.getUserNotifications(UserId(context.user.id), 10, 0)

        context.sendEphemeralMessage {
            embed {
                title = context.i18nContext.get(NotificationsCommand.I18N_PREFIX.List.Title)

                for (notification in notifications) {
                    field(
                        "[${notification.id}] ${
                        when (notification) {
                            is DailyTaxTaxedUserNotification -> context.i18nContext.get(NotificationsCommand.I18N_PREFIX.DailyTaxTaxedUserNotification)
                            is DailyTaxWarnUserNotification -> context.i18nContext.get(NotificationsCommand.I18N_PREFIX.DailyTaxWarnUserNotification)
                            is CorreiosPackageUpdateUserNotification -> context.i18nContext.get(NotificationsCommand.I18N_PREFIX.CorreiosPackageUpdate)
                            is UnknownUserNotification -> context.i18nContext.get(NotificationsCommand.I18N_PREFIX.UnknownNotification)
                        }}",
                        "<t:${notification.timestamp.epochSeconds}:d> <t:${notification.timestamp.epochSeconds}:t> | <t:${notification.timestamp.epochSeconds}:R>",
                        false
                    )
                }

                color = LorittaColors.LorittaAqua.toKordColor()
            }
        }
    }
}