package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.modules.InviteLinkModule
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import java.net.UnknownHostException
import java.util.*

class IsUpCommand : AbstractCommand("isup", category = CommandCategory.UTILS) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["ISUP_DESCRIPTION"]
	}

	override fun getExamples(): List<String> {
		return Arrays.asList("http://loritta.website/")
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			var url = context.args[0]

			val inviteBlockerConfig = context.config.inviteBlockerConfig
			val checkInviteLinks = inviteBlockerConfig.isEnabled && !inviteBlockerConfig.whitelistedChannels.contains(context.event.channel.id) && !context.lorittaUser.hasPermission(LorittaPermission.ALLOW_INVITES)

			if (checkInviteLinks) {
				val whitelisted = mutableListOf<String>()
				whitelisted.addAll(context.config.inviteBlockerConfig.whitelistedIds)

				InviteLinkModule.cachedInviteLinks[context.guild.id]?.forEach {
					whitelisted.add(it)
				}

				if (MiscUtils.hasInvite(url, whitelisted)) {
					return
				}
			}

			if (!url.startsWith("http", true)) {
				url = "http://" + url
			}
			url = url.toLowerCase()

			try {
				var response = HttpRequest.get(url)
						.userAgent(Constants.USER_AGENT)
						.connectTimeout(5000)
						.readTimeout(5000)
						.code()

				if (response in 100..308) {
					context.sendMessage(context.getAsMention(true) + context.legacyLocale["ISUP_ONLINE", url, response])
				} else {
					context.sendMessage(context.getAsMention(true) + context.legacyLocale["ISUP_OFFLINE", url, response])
				}
			} catch (e: Exception) {
				var reason = e.message
				if (e.cause is UnknownHostException) {
					reason = context.legacyLocale["ISUP_UNKNOWN_HOST", url]
				}
				context.sendMessage(context.getAsMention(true) + context.legacyLocale["ISUP_OFFLINE", url, reason])
			}
		} else {
			this.explain(context)
		}
	}
}