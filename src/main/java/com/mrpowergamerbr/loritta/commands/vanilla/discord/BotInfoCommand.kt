package com.mrpowergamerbr.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
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
		if (arg0 == "extended" || arg0 == "more" || arg0 == "mais") {
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
			if (it.reactionEmote.name == "abc") {
				message.delete().complete()

				showExtendedInfo(context, locale)
			}
		}
	}

	fun showExtendedInfo(context: CommandContext, locale: BaseLocale) {
		val path = this::class.java.protectionDomain.codeSource.location.path
		val jar = JarFile(path)
		val mf = jar.manifest
		val mattr = mf.mainAttributes

		val buildNumber = mattr[Attributes.Name("Build-Number")] as String
		val commitHash = mattr[Attributes.Name("Commit-Hash")] as String
		val gitBranch = mattr[Attributes.Name("Git-Branch")] as String
		val compiledAt = mattr[Attributes.Name("Compiled-At")] as String

		val embed = EmbedBuilder()
		embed.setColor(Color(0, 193, 223))
		embed.addField("\uD83C\uDFD7 Build Number", "[#$buildNumber](https://jenkins.perfectdreams.net/job/Loritta/$buildNumber/)", true)
		embed.addField("<:github:467329174387032086> Commit", "[$commitHash](https://github.com/LorittaBot/Loritta/commit/$commitHash)", true)
		embed.addField("<:github:467329174387032086> Git Branch", gitBranch, true)
		embed.addField("⏰ Compiled At", compiledAt, true)

		val mb = 1024 * 1024
		val runtime = Runtime.getRuntime()
		val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / mb
		val freeMemory = runtime.freeMemory() / mb
		val maxMemory = runtime.maxMemory() / mb
		val totalMemory = runtime.totalMemory() / mb

		val memory = """```diff
			|+ Memória utilizada : $usedMemory MB
			|- Memória disponível: $freeMemory MB
			|+ Memória alocada   : $totalMemory MB
			|- Memória total     : $maxMemory MB```""".trimMargin()

		embed.addField("\uD83D\uDDA5 Memória", memory, true)
		embed.addField("\uD83D\uDDA5 Thread Count", Thread.getAllStackTraces().keys.size.toString(), true)

		context.sendMessage(context.getAsMention(true), embed.build())
	}
}