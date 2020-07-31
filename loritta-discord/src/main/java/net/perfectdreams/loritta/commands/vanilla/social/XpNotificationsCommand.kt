package net.perfectdreams.loritta.commands.vanilla.social

import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.discordCommand
import net.perfectdreams.loritta.utils.Emotes

object XpNotificationsCommand {
	fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("xpnotifications"), CommandCategory.SOCIAL) {
		description { it["commands.social.xpnotifications.description"] }

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
								locale["commands.social.xpnotifications.disabledNotifications"],
								Emotes.LORI_SMILE
						)
				)
			} else {
				reply(
						LorittaReply(
								locale["commands.social.xpnotifications.enabledNotifications"],
								Emotes.LORI_SMILE
						)
				)
			}
		}
	}
}