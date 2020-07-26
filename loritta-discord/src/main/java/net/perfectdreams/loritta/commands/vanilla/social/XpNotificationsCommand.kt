package net.perfectdreams.loritta.commands.vanilla.social

import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext

class XpNotificationsCommand : LorittaCommand(arrayOf("xpnotifications"), category = CommandCategory.SOCIAL) {
    override fun getDescription(locale: BaseLocale): String? {
        return locale["commands.social.xpnotifications.description"]
    }

    @Subcommand
    suspend fun run(context: DiscordCommandContext, locale: BaseLocale) {
        val newValue = loritta.newSuspendedTransaction {
            context.lorittaUser.profile.settings.doNotSendXpNotificationsInDm = !context.lorittaUser.profile.settings.doNotSendXpNotificationsInDm

            context.lorittaUser.profile.settings.doNotSendXpNotificationsInDm
        }

        if (newValue) {
            context.reply(
                    LoriReply(
                            locale["commands.social.xpnotifications.disabledNotifications"]
                    )
            )
        } else {
            context.reply(
                    LoriReply(
                            locale["commands.social.xpnotifications.enabledNotifications"]
                    )
            )
        }
    }
}