package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.modules.InviteLinkModule
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.MiscUtils
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale

class CongaParrotCommand : AbstractCommand("congaparrot", category = CommandCategory.FUN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["CONGAPARROT_Description"]
	}

	override fun getUsage(): String {
		return "número"
	}

	override fun getExamples(): List<String> {
		return listOf("5", "10")
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		var arg0 = context.args.getOrNull(0)

		val inviteBlockerConfig = context.config.inviteBlockerConfig
		val checkInviteLinks = inviteBlockerConfig.isEnabled && !inviteBlockerConfig.whitelistedChannels.contains(context.event.channel.id) && !context.lorittaUser.hasPermission(LorittaPermission.ALLOW_INVITES)

		if (checkInviteLinks) {
			val whitelisted = mutableListOf<String>()
			whitelisted.addAll(context.config.inviteBlockerConfig.whitelistedIds)

			InviteLinkModule.cachedInviteLinks[context.guild.id]?.forEach {
				whitelisted.add(it)
			}

			if (MiscUtils.hasInvite(arg0, whitelisted)) {
				return
			}
		}

		if (arg0 == null) {
			context.explain()
			return
		}

		val upTo = arg0.toIntOrNull()

		if (upTo == null) {
			context.reply(
					LoriReply(
							message = locale["INVALID_NUMBER", context.args[0]],
							prefix = Constants.ERROR
					)
			)
			return
		}

		if (upTo in 1..50) {
			var message = ""

			for (idx in 1..upTo) {
				message += "<a:congaparrot:393804615067500544>"
			}

			context.sendMessage(message)
		} else {
			context.reply(
					LoriReply(
							message = locale["CONGAPARROT_InvalidRange"],
							prefix = Constants.ERROR

					)
			)
		}
	}
}