package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import org.apache.commons.lang3.RandomStringUtils

class LoriServerListConfigCommand : AbstractCommand("lslc", category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Configura servidores na Lori's Server List"
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val arg0 = context.rawArgs.getOrNull(0)
		val arg1 = context.rawArgs.getOrNull(1)
		val arg2 = context.rawArgs.getOrNull(2)
		val arg3 = context.rawArgs.getOrNull(3)

		if (arg0 == "set_sponsor" && arg1 != null && arg2 != null && arg3 != null) {
			val guild = lorittaShards.getGuildById(arg1)!!
			val serverConfig = loritta.getServerConfigForGuild(guild.id)
			val isSponsor = arg2.toBoolean()

			serverConfig.serverListConfig.isSponsored = isSponsor
			serverConfig.serverListConfig.sponsorPaid = arg3.toDouble()

			val rawArgs = context.rawArgs.toMutableList()
			rawArgs.removeAt(0)
			rawArgs.removeAt(0)
			rawArgs.removeAt(0)
			rawArgs.removeAt(0)

			serverConfig.serverListConfig.sponsoredUntil = rawArgs.joinToString(" ").convertToEpochMillis()

			loritta save serverConfig

			context.reply(
					LoriReply(
							"Servidor `${guild.name}` foi marcado como patrociado até `${serverConfig.serverListConfig.sponsoredUntil.humanize(locale)}`"
					)
			)
		}

		if (arg0 == "generate_key" && arg1 != null && arg2 != null) {
			val rawArgs = context.rawArgs.toMutableList()
			rawArgs.removeAt(0)

			val args = rawArgs.joinToString(" ")
					.split("|")
					.map { it.trim() }
					.toMutableList()

			val price = args[0].toDouble()
			val reason = args[1]

			val time = args[2].convertToEpochMillis()

			val key = RandomStringUtils.random(32, 0, 66, true, true, *"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890@!$&".toCharArray())

			val premiumKey = PremiumKey(
					key,
					reason,
					time,
					price
			)

			loritta.premiumKeys.add(premiumKey)

			loritta.savePremiumKeys()

			context.reply(
					LoriReply(
							"Key gerada! `${premiumKey.name}`"
					)
			)
		}

		if (arg0 == "set_donator" && arg1 != null && arg2 != null && arg3 != null) {
			val user = lorittaShards.getUserById(arg1)!!
			val userConfig = loritta.getLorittaProfileForUser(user.id)
			val isDonator = arg2.toBoolean()

			userConfig.isDonator = isDonator
			userConfig.donatorPaid = arg3.toDouble()

			val rawArgs = context.rawArgs.toMutableList()
			rawArgs.removeAt(0)
			rawArgs.removeAt(0)
			rawArgs.removeAt(0)
			rawArgs.removeAt(0)

			userConfig.donationExpiresIn = rawArgs.joinToString(" ").convertToEpochMillis()
			userConfig.donatedAt = System.currentTimeMillis()

			loritta save userConfig

			context.reply(
					LoriReply(
							"Usuário `${user.name}` foi marcado como doador até `${userConfig.donationExpiresIn.humanize(locale)}`"
					)
			)
		}
	}
}