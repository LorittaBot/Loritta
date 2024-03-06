package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.notifications

import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.NotificationsCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.NotificationUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.UserId
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot

class NotificationsViewExecutor(loritta: LorittaBot) : CinnamonSlashCommandExecutor(loritta) {
    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        val id = string("id", NotificationsCommand.I18N_PREFIX.View.Options.Id.Text)
    }

    override val options = Options()

    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        context.deferChannelMessageEphemerally()

        val notification = context.loritta.pudding.notifications.getUserNotification(UserId(context.user.id), args[options.id].toLong())
            ?: context.failEphemerally(
                prefix = Emotes.Error,
                content = context.i18nContext.get(NotificationsCommand.I18N_PREFIX.View.CouldntFindTheNotification)
            )

        context.sendEphemeralMessage {
            apply(
                NotificationUtils.buildUserNotificationMessage(
                    loritta,
                    context.i18nContext,
                    notification,
                    context.loritta.config.loritta.website.url
                )
            )
        }
    }
}