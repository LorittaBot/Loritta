package net.perfectdreams.loritta.commands.vanilla.discord

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.tables.ExecutedCommandsLog
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.utils.BuildInfo
import net.perfectdreams.loritta.utils.Emotes
import org.jetbrains.exposed.sql.select
import java.awt.Color
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import java.util.jar.Attributes
import java.util.jar.JarFile

class BotInfoCommand(loritta: LorittaDiscord, private val buildInfo: BuildInfo) : DiscordAbstractCommandBase(loritta, listOf("botinfo"), CommandCategory.DISCORD) {
	companion object {
		private const val LOCALE_PREFIX = "commands.discord.botinfo"
	}

	override fun command() = create {
		localizedDescription("$LOCALE_PREFIX.description")

		executesDiscord {
			val context = this

			val arg0 = context.args.getOrNull(0)
			if (arg0 == "extended" || arg0 == "more" || arg0 == "mais" || arg0 == "extendedinfo") {
				showExtendedInfo(context, locale)
				return@executesDiscord
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

			val commandsExecutedInTheLast24Hours = loritta.newSuspendedTransaction {
				ExecutedCommandsLog.select {
					ExecutedCommandsLog.sentAt greaterEq (System.currentTimeMillis() - Constants.ONE_DAY_IN_MILLISECONDS)
				}.count()
			}

			embed.setAuthor("${context.locale["$LOCALE_PREFIX.title"]} 💁", loritta.instanceConfig.loritta.website.url, "${loritta.instanceConfig.loritta.website.url}assets/img/loritta_gabizinha_v1.png")
			embed.setThumbnail("${loritta.instanceConfig.loritta.website.url}assets/img/loritta_gabizinha_v1.png")
			embed.setColor(Color(0, 193, 223))
			embed.setDescription(
					context.locale.getList(
							"$LOCALE_PREFIX.embedDescription",
							guildCount,
							sb.toString(),
							LorittaLauncher.loritta.legacyCommandManager.commandMap.size + loritta.commandMap.commands.size,
							commandsExecutedInTheLast24Hours,
							Emotes.KOTLIN,
							Emotes.JDA,
							Emotes.LORI_SMILE,
							Emotes.LORI_HAPPY,
							Emotes.LORI_OWO
					).joinToString("\n\n")
			)

			embed.addField("\uD83C\uDF80 ${context.locale["website.donate.title"]}", "${loritta.instanceConfig.loritta.website.url}donate", true)
			embed.addField("<:loritta:331179879582269451> ${context.locale["website.jumbotron.addMe"]}", "${loritta.instanceConfig.loritta.website.url}dashboard", true)
			embed.addField("<:lori_ok_hand:426183783008698391> ${context.locale["modules.sectionNames.commands"]}", "${loritta.instanceConfig.loritta.website.url}commands", true)
			embed.addField("\uD83D\uDC81 ${context.locale["website.support.title"]}", "${loritta.instanceConfig.loritta.website.url}support", true)
			embed.addField(locale["$LOCALE_PREFIX.crowdin"], loritta.config.crowdin.url, true)
			embed.addField("<:twitter:552840901886738433> Twitter", "[@LorittaBot](https://twitter.com/LorittaBot)", true)
			embed.addField("<:instagram:552841049660325908> Instagram", "[@lorittabot](https://instagram.com/lorittabot/)", true)

			val numberOfUniqueDonators = com.mrpowergamerbr.loritta.utils.loritta.newSuspendedTransaction {
				Payments.slice(Payments.userId)
						.select { Payments.paidAt.isNotNull() }
						.groupBy(Payments.userId)
						.count()
			}

			embed.addField(
					"\uD83C\uDFC5 ${locale["$LOCALE_PREFIX.honorableMentionsTitle"]}",
					locale.getList(
							"$LOCALE_PREFIX.honorableMentions",
							numberOfUniqueDonators,
							com.mrpowergamerbr.loritta.utils.loritta.fanArtArtists.size,
							context.user.asMention,
							Emotes.LORI_TEMMIE,
							Emotes.LORI_OWO,
							Emotes.LORI_WOW,
							Emotes.LORI_HUG,
							Emotes.LORI_SMILE
					).joinToString("\n") { "• $it" },
					false
			)

			embed.setFooter("${locale["commands.discord.botinfo.lorittaCreatedBy"]} - https://mrpowergamerbr.com/", lorittaShards.retrieveUserById(123170274651668480L)!!.effectiveAvatarUrl)

			val message = context.sendMessage(context.getUserMention(true), embed.build())

			message.onReactionAddByAuthor(context) {
				if (it.reactionEmote.isEmote("loritta")) {
					message.delete().queue()

					showExtendedInfo(context, locale)
				}
			}

			message.addReaction("loritta:331179879582269451").queue()
		}
	}

	suspend fun showExtendedInfo(context: DiscordCommandContext, locale: BaseLocale) {
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

		val buildURL = getBuildURL()

		context.reply(
				LorittaReply(
						forceMention = true,
						prefix = "<:loritta:331179879582269451>"
				),
				LorittaReply(
						"**${locale["$LOCALE_PREFIX.lorittaVersion"]}:** $lorittaVersion",
						"\uD83D\uDD16",
						mentionUser = false
				),
				LorittaReply(
						"**${locale["$LOCALE_PREFIX.buildNumber"]}:** #$buildNumber <$buildURL>",
						"\uD83C\uDFD7",
						mentionUser = false
				),
				LorittaReply(
						"**Commit:** $commitHash",
						"<:github:467329174387032086>",
						mentionUser = false
				),
				LorittaReply(
						"**Git Branch:** $gitBranch",
						"<:github:467329174387032086>",
						mentionUser = false
				),
				LorittaReply(
						"**${locale["$LOCALE_PREFIX.compiledAt"]}:** $compiledAt",
						"⏰",
						mentionUser = false
				),
				LorittaReply(
						"**${locale["$LOCALE_PREFIX.javaVersion"]}:** ${System.getProperty("java.version")}",
						"<:java:467443707160035329>",
						mentionUser = false
				),
				LorittaReply(
						"**${locale["$LOCALE_PREFIX.kotlinVersion"]}:** $kotlinVersion",
						"<:kotlin:453714186925637642>",
						mentionUser = false
				),
				LorittaReply(
						"**${locale["$LOCALE_PREFIX.jdaVersion"]}:** $jdaVersion",
						"<:jda:411518264267767818>",
						mentionUser = false
				),
				LorittaReply(
						"**${locale["$LOCALE_PREFIX.memoryUsed"]}:** $usedMemory MB",
						"\uD83D\uDCBB",
						mentionUser = false
				),
				LorittaReply(
						"**${locale["$LOCALE_PREFIX.memoryAvailable"]}:** $freeMemory MB",
						"\uD83D\uDCBB",
						mentionUser = false
				),
				LorittaReply(
						"**${locale["$LOCALE_PREFIX.memoryAllocated"]}:** $totalMemory MB",
						"\uD83D\uDCBB",
						mentionUser = false
				),
				LorittaReply(
						"**${locale["$LOCALE_PREFIX.memoryTotal"]}:** $maxMemory MB",
						"\uD83D\uDCBB",
						mentionUser = false
				),
				LorittaReply(
						"**${locale["$LOCALE_PREFIX.threadCount"]}:** ${ManagementFactory.getThreadMXBean().threadCount}",
						"\uD83D\uDC4B",
						mentionUser = false
				),
				LorittaReply(
						"**${locale["$LOCALE_PREFIX.environment"]}:** ${loritta.config.loritta.environment.name}",
						"\uD83C\uDF43",
						mentionUser = false
				),
				LorittaReply(
						"**${locale["$LOCALE_PREFIX.love"]}:** ∞",
						"<:blobheart:467447056374693889>",
						mentionUser = false
				)
		)
	}

	private fun getBuildURL() = buildInfo.buildUrl()
}