package net.perfectdreams.loritta.morenitta.commands.vanilla.social

import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.common.utils.Emotes

class XpNotificationsCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("xpnotifications"), net.perfectdreams.loritta.common.commands.CommandCategory.SOCIAL) {
	override fun command() = create {
		localizedDescription("commands.command.xpnotifications.description")

		arguments {
			argument(ArgumentType.NUMBER) {
				optional = true
			}
		}

		executesDiscord {
			val newValue = loritta.newSuspendedTransaction {
				lorittaUser.profile.settings.doNotSendXpNotificationsInDm = !lorittaUser.profile.settings.doNotSendXpNotificationsInDm

				lorittaUser.profile.settings.doNotSendXpNotificationsInDm
			}

			if (newValue) {
				reply(
						LorittaReply(
								locale["commands.command.xpnotifications.disabledNotifications"],
								Emotes.LORI_SMILE
						)
				)
			} else {
				reply(
						LorittaReply(
								locale["commands.command.xpnotifications.enabledNotifications"],
								Emotes.LORI_SMILE
						)
				)
			}
		}
	}
}