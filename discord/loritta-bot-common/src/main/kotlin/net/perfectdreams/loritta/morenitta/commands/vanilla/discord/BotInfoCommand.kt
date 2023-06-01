package net.perfectdreams.loritta.morenitta.commands.vanilla.discord

import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.HostnameUtils
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.vanilla.discord.LorittaCommand
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import java.lang.management.ManagementFactory

class BotInfoCommand(loritta: LorittaBot) : AbstractCommand(loritta, "botinfo", category = net.perfectdreams.loritta.common.commands.CommandCategory.DISCORD) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.botinfo.description")

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		val arg0 = context.rawArgs.getOrNull(0)
		if (arg0 == "extended" || arg0 == "more" || arg0 == "mais" || arg0 == "extendedinfo") {
			showExtendedInfo(context, locale)
			return
		}

		OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "loritta info")

		LorittaCommand.executeCompat(CommandContextCompat.LegacyMessageCommandContextCompat(context))
		return
	}

	suspend fun showExtendedInfo(context: CommandContext, locale: BaseLocale) {
		val mb = 1024 * 1024
		val runtime = Runtime.getRuntime()
		val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / mb
		val freeMemory = runtime.freeMemory() / mb
		val maxMemory = runtime.maxMemory() / mb
		val totalMemory = runtime.totalMemory() / mb

		val buildNumber = System.getenv("BUILD_ID")
		val commitHash = System.getenv("COMMIT_HASH")

		context.reply(
			LorittaReply(
				forceMention = true,
				prefix = "<:loritta:331179879582269451>"
			),
			LorittaReply(
				"**Hostname:** `${HostnameUtils.getHostname()}`",
				"\uD83C\uDFD7",
				mentionUser = false
			),
			LorittaReply(
				"**${locale["commands.command.botinfo.buildNumber"]}:** #$buildNumber",
				"\uD83C\uDFD7",
				mentionUser = false
			),
			LorittaReply(
				"**Commit:** $commitHash",
				"<:github:467329174387032086>",
				mentionUser = false
			),
			LorittaReply(
				"**${locale["commands.command.botinfo.javaVersion"]}:** ${System.getProperty("java.version")}",
				"<:java:467443707160035329>",
				mentionUser = false
			),
			LorittaReply(
				"**${locale["commands.command.botinfo.kotlinVersion"]}:** ${KotlinVersion.CURRENT}",
				"<:kotlin:453714186925637642>",
				mentionUser = false
			),
			LorittaReply(
				"**${locale["commands.command.botinfo.memoryUsed"]}:** $usedMemory MB",
				"\uD83D\uDCBB",
				mentionUser = false
			),
			LorittaReply(
				"**${locale["commands.command.botinfo.memoryAvailable"]}:** $freeMemory MB",
				"\uD83D\uDCBB",
				mentionUser = false
			),
			LorittaReply(
				"**${locale["commands.command.botinfo.memoryAllocated"]}:** $totalMemory MB",
				"\uD83D\uDCBB",
				mentionUser = false
			),
			LorittaReply(
				"**${locale["commands.command.botinfo.memoryTotal"]}:** $maxMemory MB",
				"\uD83D\uDCBB",
				mentionUser = false
			),
			LorittaReply(
				"**${locale["commands.command.botinfo.threadCount"]}:** ${ManagementFactory.getThreadMXBean().threadCount}",
				"\uD83D\uDC4B",
				mentionUser = false
			),
			LorittaReply(
				"**${locale["commands.command.botinfo.environment"]}:** ${loritta.config.loritta.environment.name}",
				"\uD83C\uDF43",
				mentionUser = false
			),
			LorittaReply(
				"**${locale["commands.command.botinfo.love"]}:** ∞",
				"<:blobheart:467447056374693889>",
				mentionUser = false
			)
		)
	}
}
