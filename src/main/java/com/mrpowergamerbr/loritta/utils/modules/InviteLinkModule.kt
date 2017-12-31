package com.mrpowergamerbr.loritta.utils.modules

import com.mrpowergamerbr.loritta.userdata.InviteBlockerConfig
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import com.mrpowergamerbr.loritta.utils.GuildLorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.MiscUtils
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import java.util.regex.Pattern

object InviteLinkModule {
	fun checkForInviteLinks(message: Message, guild: Guild, lorittaProfile: GuildLorittaUser, permissionsConfig: PermissionsConfig, inviteBlockerConfig: InviteBlockerConfig): Boolean {
		if (inviteBlockerConfig.whitelistedChannels.contains(message.channel.id))
			return false

		if (lorittaProfile.hasPermission(LorittaPermission.ALLOW_INVITES))
			return false

		val content = message.content

		val whitelisted = mutableListOf<String>()
		inviteBlockerConfig.whitelistedIds.clear()
		whitelisted.addAll(inviteBlockerConfig.whitelistedIds)
		if (inviteBlockerConfig.whitelistServerInvites && guild.selfMember.hasPermission(Permission.MANAGE_SERVER)) {
			guild.invites.complete().forEach {
				whitelisted.add(it.code)
			}
		}

		val matcher = Pattern.compile("[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)").matcher(content)

		while (matcher.find()) {
			var url = matcher.group()
			if (url.contains("discord") && url.contains("gg")) {
				url = "discord.gg" + matcher.group(1).replace(".", "")
			}
			val inviteId = MiscUtils.getInviteId("https://$url") ?: continue

			if (inviteId == "attachments")
				continue

			if (whitelisted.contains(inviteId))
				continue

			val asMention = message.author.asMention
			val name = message.author.name
			val effectiveName = message.member.effectiveName

			if (inviteBlockerConfig.deleteMessage && guild.selfMember.hasPermission(message.textChannel, Permission.MESSAGE_MANAGE))
				message.delete().queue()

			if (inviteBlockerConfig.tellUser && inviteBlockerConfig.warnMessage.isNotEmpty())
				message.textChannel.sendMessage(inviteBlockerConfig.warnMessage
						.replace("{@user}", asMention)
						.replace("{user}", name)
						.replace("{nickname}", effectiveName)).queue()
			return true
		}
		return false
	}
}