package com.mrpowergamerbr.loritta.utils.modules

import com.mrpowergamerbr.loritta.userdata.InviteBlockerConfig
import com.mrpowergamerbr.loritta.utils.MiscUtils
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.util.regex.Pattern

object InviteLinkUtils {
	fun checkForInviteLinks(event: MessageReceivedEvent, inviteBlockerConfig: InviteBlockerConfig) {
		if (inviteBlockerConfig.whitelistedChannels.contains(event.message.channel.id))
			return

		val message = event.message.content

		val whitelisted = inviteBlockerConfig.whitelistedIds
		if (inviteBlockerConfig.whitelistServerInvites) {
			event.guild.invites.complete().forEach {
				whitelisted.add(it.code)
			}
		}

		val matcher = Pattern.compile("[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)").matcher(message)

		while (matcher.find()) {
			val url = matcher.group()
			val inviteId = MiscUtils.getInviteId("https://$url") ?: continue

			if (whitelisted.contains(inviteId))
				continue
			return
		}
	}
}