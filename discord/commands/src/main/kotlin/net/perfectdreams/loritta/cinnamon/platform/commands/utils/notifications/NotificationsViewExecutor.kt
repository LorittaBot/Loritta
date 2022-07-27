package net.perfectdreams.loritta.cinnamon.platform.commands.utils.notifications

import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations.NotificationsCommand
import net.perfectdreams.loritta.cinnamon.platform.utils.NotificationUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.UserId

class NotificationsViewExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val id = string("id", NotificationsCommand.I18N_PREFIX.View.Options.Id.Text)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally()

        val notification = context.loritta.services.notifications.getUserNotification(UserId(context.user.id), args[options.id].toLong())
            ?: context.failEphemerally(
                prefix = Emotes.Error,
                content = context.i18nContext.get(NotificationsCommand.I18N_PREFIX.View.CouldntFindTheNotification)
            )

        context.sendEphemeralMessage {
            apply(
                NotificationUtils.buildUserNotificationMessage(
                    context.i18nContext,
                    notification,
                    context.loritta.config.website
                )
            )
        }
    }
}