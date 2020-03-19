package net.perfectdreams.loritta.commands.vanilla.magic

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.command
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin

object LoriToolsCommand {
	fun create(loritta: LorittaBot) = command(loritta, "LoriToolsCommand", listOf("loritools"), CommandCategory.MAGIC) {
		description { "Ferramentas de Administração da Loritta" }
		onlyOwner = true

		executes {
			val validPlugins = loritta.pluginManager.plugins.filterIsInstance<LorittaDiscordPlugin>()

			val allExecutors = listOf(
					RegisterTwitchChannelExecutor,
					RegisterYouTubeChannelExecutor,
					PurgeInactiveGuildsExecutor,
					PurgeInactiveUsersExecutor,
					PurgeInactiveGuildUsersExecutor,
					SetSelfBackgroundExecutor,
					GenerateDailyShopExecutor,
					PriceCorrectionExecutor
			) + validPlugins.flatMap { it.loriToolsExecutors }

			allExecutors.forEach {
				val result = it.executes().invoke(this)

				if (result)
					return@executes
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