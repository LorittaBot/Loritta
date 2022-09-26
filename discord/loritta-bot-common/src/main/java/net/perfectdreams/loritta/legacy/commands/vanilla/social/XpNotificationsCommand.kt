package net.perfectdreams.loritta.legacy.commands.vanilla.social

import net.perfectdreams.loritta.legacy.api.commands.ArgumentType
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.api.commands.arguments
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.legacy.utils.Emotes

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