package net.perfectdreams.loritta.commands.vanilla.social

import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.Emotes

class XpNotificationsCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("xpnotifications"), CommandCategory.SOCIAL) {
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