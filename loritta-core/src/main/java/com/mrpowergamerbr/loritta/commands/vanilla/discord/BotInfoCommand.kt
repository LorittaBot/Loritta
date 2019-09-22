package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import java.util.jar.Attributes
import java.util.jar.JarFile

class BotInfoCommand : AbstractCommand("botinfo", category = CommandCategory.DISCORD) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("BOTINFO_DESCRIPTION")
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		val arg0 = context.rawArgs.getOrNull(0)
		if (arg0 == "extended" || arg0 == "more" || arg0 == "mais" || arg0 == "extendedinfo") {
			showExtendedInfo(context, locale)
			return
		}

		val guildCount = lorittaShards.queryGuildCount()

		val embed = EmbedBuilder()

		var jvmUpTime = ManagementFactory.getRuntimeMXBean().uptime

		val days = TimeUnit.MILLISECONDS.toDays(jvmUpTime)
		jvmUpTime -= TimeUnit.DAYS.toMillis(days)
		val hours = TimeUnit.MILLISECONDS.toHours(jvmUpTime)
		jvmUpTime -= TimeUnit.HOURS.toMillis(hours)
		val minutes = TimeUnit.MILLISECONDS.toMinutes(jvmUpTime)
		jvmUpTime -= TimeUnit.MINUTES.toMillis(minutes)
		val seconds = TimeUnit.MILLISECONDS.toSeconds(jvmUpTime)

		val sb = StringBuilder(64)
		sb.append(days)
		sb.append("d ")
		sb.append(hours)
		sb.append("h ")
		sb.append(minutes)
		sb.append("m ")
		sb.append(seconds)
		sb.append("s")

		embed.setAuthor("${locale["BOTINFO_TITLE"]} 💁", loritta.instanceConfig.loritta.website.url, "${loritta.instanceConfig.loritta.website.url}assets/img/loritta_gabizinha_v1.png")
		embed.setThumbnail("${loritta.instanceConfig.loritta.website.url}assets/img/loritta_gabizinha_v1.png")
		embed.setColor(Color(0, 193, 223))
		embed.setDescription(
				locale.toNewLocale().getWithType<List<String>>("commands.discord.botinfo.embedDescription")
						.joinToString("\n\n")
						.msgFormat(
								guildCount,
								sb.toString(),
								LorittaLauncher.loritta.legacyCommandManager.commandMap.size + loritta.commandManager.commands.size,
								LorittaUtilsKotlin.executedCommands,
								Emotes.KOTLIN,
								Emotes.JDA,
								Emotes.LORI_SMILE,
								Emotes.LORI_HAPPY,
								Emotes.LORI_OWO
						)
		)

		embed.addField("\uD83C\uDF80 ${context.legacyLocale["WEBSITE_DONATE"]}", "${loritta.instanceConfig.loritta.website.url}donate", true)
		embed.addField("<:loritta:331179879582269451> ${context.legacyLocale["WEBSITE_ADD_ME"]}", "${loritta.instanceConfig.loritta.website.url}dashboard", true)
		embed.addField("<:lori_ok_hand:426183783008698391> ${context.legacyLocale["WEBSITE_COMMANDS"]}", "${loritta.instanceConfig.loritta.website.url}commands", true)
		embed.addField("\uD83D\uDC81 ${context.legacyLocale["WEBSITE_Support"]}", "${loritta.instanceConfig.loritta.website.url}support", true)
		embed.addField("<:twitter:552840901886738433> Twitter", "[@LorittaBot](https://twitter.com/LorittaBot)", true)
		embed.addField("<:instagram:552841049660325908> Instagram", "[@lorittabot](https://instagram.com/lorittabot/)", true)

		val numberOfUniqueDonators = transaction(Databases.loritta) {
			Payments.slice(Payments.userId)
					.select { Payments.paidAt.isNotNull() }
					.groupBy(Payments.userId)
					.count()
		}

		embed.addField(
				"\uD83C\uDFC5 ${locale["BOTINFO_HONORABLE_MENTIONS"]}",
				locale.toNewLocale().getWithType<List<String>>("commands.discord.botinfo.honorableMentions").joinToString("\n") { "• $it" }
						.msgFormat(
								numberOfUniqueDonators,
								loritta.fanArtArtists.size,
								context.userHandle.asMention,
								Emotes.LORI_TEMMIE,
								Emotes.LORI_OWO,
								Emotes.LORI_WOW,
								Emotes.LORI_HUG,
								Emotes.LORI_SMILE
						),
				false
		)

		embed.setFooter("${locale["BOTINFO_CREATEDBY"]} - https://mrpowergamerbr.com/", lorittaShards.getUserById("123170274651668480")!!.effectiveAvatarUrl)
		val message = context.sendMessage(context.getAsMention(true), embed.build())

		message.onReactionAddByAuthor(context) {
			if (it.reactionEmote.isEmote("loritta")) {
				message.delete().queue()

				showExtendedInfo(context, locale)
			}
		}

		message.addReaction("loritta:331179879582269451").queue()
	}

	suspend fun showExtendedInfo(context: CommandContext, locale: LegacyBaseLocale) {
		val path = this::class.java.protectionDomain.codeSource.location.path
		val jar = JarFile(path)
		val mf = jar.manifest
		val mattr = mf.mainAttributes

		val lorittaVersion = mattr[Attributes.Name("Loritta-Version")] as String
		val buildNumber = mattr[Attributes.Name("Build-Number")] as String
		val commitHash = mattr[Attributes.Name("Commit-Hash")] as String
		val gitBranch = mattr[Attributes.Name("Git-Branch")] as String
		val compiledAt = mattr[Attributes.Name("Compiled-At")] as String
		val kotlinVersion = mattr[Attributes.Name("Kotlin-Version")] as String
		val jdaVersion = mattr[Attributes.Name("JDA-Version")] as String

		val mb = 1024 * 1024
		val runtime = Runtime.getRuntime()
		val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / mb
		val freeMemory = runtime.freeMemory() / mb
		val maxMemory = runtime.maxMemory() / mb
		val totalMemory = runtime.totalMemory() / mb

		context.reply(
				LoriReply(
						forceMention = true,
						prefix = "<:loritta:331179879582269451>"
				),
				LoriReply(
						"**${locale["BOTINFO_LorittaVersion"]}:** $lorittaVersion",
						"\uD83D\uDD16",
						mentionUser = false
				),
				LoriReply(
						"**${locale["BOTINFO_BuildNumber"]}:** #$buildNumber <https://jenkins.perfectdreams.net/job/Loritta/$buildNumber/>",
						"\uD83C\uDFD7",
						mentionUser = false
				),
				LoriReply(
						"**Commit:** $commitHash",
						"<:github:467329174387032086>",
						mentionUser = false
				),
				LoriReply(
						"**Git Branch:** $gitBranch",
						"<:github:467329174387032086>",
						mentionUser = false
				),
				LoriReply(
						"**${locale["BOTINFO_CompiledAt"]}:** $compiledAt",
						"⏰",
						mentionUser = false
				),
				LoriReply(
						"**${locale["BOTINFO_JavaVersion"]}:** ${System.getProperty("java.version")}",
						"<:java:467443707160035329>",
						mentionUser = false
				),
				LoriReply(
						"**${locale["BOTINFO_KotlinVersion"]}:** $kotlinVersion",
						"<:kotlin:453714186925637642>",
						mentionUser = false
				),
				LoriReply(
						"**${locale["BOTINFO_JDAVersion"]}:** $jdaVersion",
						"<:jda:411518264267767818>",
						mentionUser = false
				),
				LoriReply(
						"**${locale["BOTINFO_MemoryUsed"]}:** $usedMemory MB",
						"\uD83D\uDCBB",
						mentionUser = false
				),
				LoriReply(
						"**${locale["BOTINFO_MemoryAvailable"]}:** $freeMemory MB",
						"\uD83D\uDCBB",
						mentionUser = false
				),
				LoriReply(
						"**${locale["BOTINFO_MemoryAllocated"]}:** $totalMemory MB",
						"\uD83D\uDCBB",
						mentionUser = false
				),
				LoriReply(
						"**${locale["BOTINFO_MemoryTotal"]}:** $maxMemory MB",
						"\uD83D\uDCBB",
						mentionUser = false
				),
				LoriReply(
						"**${locale["BOTINFO_ThreadCount"]}:** ${ManagementFactory.getThreadMXBean().threadCount}",
						"\uD83D\uDC4B",
						mentionUser = false
				),
				LoriReply(
						"**${locale["BOTINFO_Environment"]}:** ${loritta.config.loritta.environment.name}",
						"\uD83C\uDF43",
						mentionUser = false
				),
				LoriReply(
						"**${locale["DASHBOARD_Love"]}:** ∞",
						"<:blobheart:467447056374693889>",
						mentionUser = false
				)
		)
	}
}