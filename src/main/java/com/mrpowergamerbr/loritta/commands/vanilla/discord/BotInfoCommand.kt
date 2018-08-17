package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import java.util.jar.Attributes
import java.util.jar.JarFile

class BotInfoCommand : AbstractCommand("botinfo", category = CommandCategory.DISCORD) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("BOTINFO_DESCRIPTION")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val arg0 = context.rawArgs.getOrNull(0)
		if (arg0 == "extended" || arg0 == "more" || arg0 == "mais" || arg0 == "extendedinfo") {
			showExtendedInfo(context, locale)
			return
		}

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

		embed.setAuthor("${locale["BOTINFO_TITLE"]} 💁", Loritta.config.websiteUrl, "${Loritta.config.websiteUrl}assets/img/loritta_gabizinha_v1.png")
		embed.setThumbnail("${Loritta.config.websiteUrl}assets/img/loritta_gabizinha_v1.png")
		embed.setColor(Color(0, 193, 223))
		embed.setDescription(locale["BOTINFO_EMBED_INFO", lorittaShards.getGuildCount(), LorittaLauncher.loritta.lorittaShards.getUserCount(), sb.toString(), LorittaLauncher.loritta.commandManager.commandMap.size, lorittaShards.getChannelCount(), lorittaShards.getEmoteCount(), LorittaUtilsKotlin.executedCommands])
		embed.addField("\uD83C\uDF80 ${context.locale["WEBSITE_DONATE"]}", "${Loritta.config.websiteUrl}donate", true)
		embed.addField("<:loritta:331179879582269451> ${context.locale["WEBSITE_ADD_ME"]}", "${Loritta.config.websiteUrl}dashboard", true)
		embed.addField("<:lori_ok_hand:426183783008698391> ${context.locale["WEBSITE_COMMANDS"]}", "${Loritta.config.websiteUrl}commands", true)
		embed.addField("\uD83D\uDC81 ${context.locale["WEBSITE_Support"]}", "${Loritta.config.websiteUrl}support", true)
		embed.addField("\uD83C\uDFC5 ${context.locale.get("BOTINFO_HONORABLE_MENTIONS")}", context.locale.get("BOTINFO_MENTIONS", context.userHandle.name, context.userHandle.discriminator), false)
		embed.setFooter("${locale["BOTINFO_CREATEDBY"]} - https://mrpowergamerbr.com/", lorittaShards.getUserById("123170274651668480")!!.effectiveAvatarUrl)
		val message = context.sendMessage(context.getAsMention(true), embed.build())

		message.onReactionAddByAuthor(context) {
			if (it.reactionEmote.name == "loritta") {
				message.delete().complete()

				showExtendedInfo(context, locale)
			}
		}

		message.addReaction("loritta:331179879582269451").complete()
	}

	fun showExtendedInfo(context: CommandContext, locale: BaseLocale) {
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
						"**${locale["BOTINFO_ThreadCount"]}:** ${Thread.getAllStackTraces().keys.size.toString()}",
						"\uD83D\uDC4B",
						mentionUser = false
				),
				LoriReply(
						"**${locale["BOTINFO_Environment"]}:** ${Loritta.config.environment.name}",
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