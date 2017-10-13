package com.mrpowergamerbr.loritta.utils.modules

import com.mrpowergamerbr.loritta.userdata.InviteBlockerConfig
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import com.mrpowergamerbr.loritta.utils.GuildLorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.MiscUtils
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.util.regex.Pattern

object InviteLinkUtils {
	fun checkForInviteLinks(event: MessageReceivedEvent, lorittaProfile: GuildLorittaUser, permissionsConfig: PermissionsConfig, inviteBlockerConfig: InviteBlockerConfig) {
		if (inviteBlockerConfig.whitelistedChannels.contains(event.message.channel.id))
			return

		if (lorittaProfile.hasPermission(LorittaPermission.ALLOW_INVITES))
			return

		val message = event.message.content

		val whitelisted = mutableListOf<String>()
		inviteBlockerConfig.whitelistedIds.clear()
		whitelisted.addAll(inviteBlockerConfig.whitelistedIds)
		if (inviteBlockerConfig.whitelistServerInvites) {
			event.guild.invites.complete().forEach {
				whitelisted.add(it.code)
			}
		}

		val matcher = Pattern.compile("[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)").matcher(message)

		while (matcher.find()) {
			val url = matcher.group()
			val inviteId = MiscUtils.getInviteId("https://$url") ?: continue

			if (inviteId == "attachments")
				continue

			if (whitelisted.contains(inviteId))
				continue

			val asMention = event.author.asMention
			val name = event.author.name
			val effectiveName = event.member.effectiveName

			if (inviteBlockerConfig.deleteMessage)
				event.message.delete().queue()

			if (inviteBlockerConfig.tellUser && inviteBlockerConfig.warnMessage.isNotEmpty())
				event.textChannel.sendMessage(inviteBlockerConfig.warnMessage
						.replace("{@user}", asMention)
						.replace("{user}", name)
						.replace("{nickname}", effectiveName)).queue()
			return
		}
	}
}