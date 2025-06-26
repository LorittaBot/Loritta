package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase

class LoriToolsCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("loritools"), net.perfectdreams.loritta.common.commands.CommandCategory.MAGIC) {
	companion object {
		private val logger by HarmonyLoggerFactory.logger {}
	}

	override fun command() = create {
		description { "Ferramentas de Administração da Loritta" }
		onlyOwner = true

		executesDiscord {
			logger.info { "Executing Lori Tools!" }

			val allExecutors = listOf(
				RegisterYouTubeChannelExecutor,
				PurgeInactiveGuildsExecutor,
				PurgeInactiveUsersExecutor,
				PurgeInactiveGuildUsersExecutor,
				SetSelfBackgroundExecutor,
				SetSelfProfileDesignExecutor,
				GenerateDailyShopExecutor,
				PriceCorrectionExecutor,
				LoriBanIpExecutor,
				LoriUnbanIpExecutor,
				DeleteAccountDataExecutor,
				ChargebackRunExecutor,
				EnableBoostExecutor,
				DisableBoostExecutor
			)

			allExecutors.forEach {
				val result = it.executes().invoke(this)

				if (result) {
					logger.info { "Executed ${it::class.simpleName} Executor!" }
					return@executesDiscord
				}
			}

			val replies = mutableListOf<LorittaReply>()

			allExecutors.forEach {
				replies.add(
					LorittaReply(
						"`${it.args}`",
						mentionUser = false
					)
				)
			}

			reply(replies)
		}
	}

	interface LoriToolsExecutor {
		val args: String

		fun executes(): (suspend CommandContext.() -> (Boolean))
	}
}