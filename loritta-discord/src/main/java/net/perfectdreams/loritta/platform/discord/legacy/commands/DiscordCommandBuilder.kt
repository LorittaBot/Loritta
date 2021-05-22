package net.perfectdreams.loritta.platform.discord.legacy.commands

import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.LorittaUser
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.CommandBuilder
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.platform.discord.LorittaDiscord

fun Any?.discordCommand(
		loritta: LorittaDiscord,
		labels: List<String>,
		category: CommandCategory,
		builder: DiscordCommandBuilder.() -> (Unit)
) = discordCommand(loritta, this?.let { this::class.simpleName } ?: "UnknownCommand", labels, category, builder)

fun discordCommand(
		loritta: LorittaDiscord,
		commandName: String,
		labels: List<String>,
		category: CommandCategory,
		builder: DiscordCommandBuilder.() -> (Unit)
): DiscordCommand {
	val b = DiscordCommandBuilder(loritta, commandName, labels, category)
	builder.invoke(b)
	return b.buildDiscord()
}

class DiscordCommandBuilder(
		// Needs to be private to avoid accessing this variable on the builder itself
		private val lorittaDiscord: LorittaDiscord,
		commandName: String,
		labels: List<String>,
		category: CommandCategory
) : CommandBuilder<CommandContext>(lorittaDiscord, commandName, labels, category) {
	var userRequiredPermissions = listOf<Permission>()
	var botRequiredPermissions = listOf<Permission>()
	var executeDiscordCallback: (suspend DiscordCommandContext.() -> (Unit))? = null
	var userRequiredLorittaPermissions = listOf<LorittaPermission>()
	var commandCheckFilter: (suspend (LorittaMessageEvent, List<String>, ServerConfig, BaseLocale, LorittaUser) -> (Boolean))? = null

	fun executesDiscord(callback: suspend DiscordCommandContext.() -> (Unit)) {
		this.executeDiscordCallback = callback
	}

	/**
	 * Sets a command check filter, this is used to enable/disable commands in specific guilds. Useful for guild-specific commands
	 *
	 * If not set, the check will always return true.
	 *
	 * @return if the command is enabled for processing or not
	 */
	fun commandCheckFilter(callback: suspend (LorittaMessageEvent, List<String>, ServerConfig, BaseLocale, LorittaUser) -> (Boolean)) {
		this.commandCheckFilter = callback
	}

	fun buildDiscord(): DiscordCommand {
		val usage = arguments {
			usageCallback?.invoke(this)
		}

		executes {
			val context = checkType<DiscordCommandContext>(this)

			executeDiscordCallback?.invoke(context)
		}

		return DiscordCommand(
				lorittaDiscord = lorittaDiscord,
				commandName = commandName,
				category = category,
				labels = labels,
				descriptionKey = builderDescriptionKey,
				description = descriptionCallback ?: {
					it.get(builderDescriptionKey)
				},
				usage = usage,
				examplesKey = builderExamplesKey,
				executor = executeCallback!!
		).apply { build2().invoke(this) }.also {
			it.userRequiredPermissions = userRequiredPermissions
			it.botRequiredPermissions = botRequiredPermissions
			it.userRequiredLorittaPermissions = userRequiredLorittaPermissions
			it.commandCheckFilter = commandCheckFilter
		}
	}
}