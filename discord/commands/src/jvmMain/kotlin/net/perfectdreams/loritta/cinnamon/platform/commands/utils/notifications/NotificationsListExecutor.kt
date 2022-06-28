package net.perfectdreams.loritta.cinnamon.platform.commands.utils.notifications

import net.perfectdreams.discordinteraktions.common.builder.message.embed
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.loritta.cinnamon.common.utils.LorittaColors
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.utils.UserId
import net.perfectdreams.loritta.cinnamon.platform.utils.toKordColor
import net.perfectdreams.loritta.cinnamon.pudding.data.CorreiosPackageUpdateUserNotification
import net.perfectdreams.loritta.cinnamon.pudding.data.DailyTaxTaxedUserNotification
import net.perfectdreams.loritta.cinnamon.pudding.data.DailyTaxWarnUserNotification
import net.perfectdreams.loritta.cinnamon.pudding.data.UnknownUserNotification

class NotificationsListExecutor : SlashCommandExecutor() {
    companion object : SlashCommandExecutorDeclaration()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally()

        val notifications = context.loritta.services.notifications.getUserNotifications(UserId(context.user.id), 10, 0)

        context.sendEphemeralMessage {
            embed {
                title = "Notifications"

                for (notification in notifications) {
                    field(
                        "[${notification.id}] ${
                        when (notification) {
                            is DailyTaxTaxedUserNotification -> "Imposto de Inatividade Diária (Taxado)"
                            is DailyTaxWarnUserNotification -> "Imposto de Inatividade Diária (Aviso)"
                            is CorreiosPackageUpdateUserNotification -> "Atualização sobre Pacote"
                            is UnknownUserNotification -> "Notification Desconhecida"
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