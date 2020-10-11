package net.perfectdreams.loritta.commands.vanilla.magic

import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin

class LoriToolsCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("loritools"), CommandCategory.MAGIC) {
	override fun command() = create {
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
					PriceCorrectionExecutor,
					LoriBanIpExecutor,
					LoriUnbanIpExecutor
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