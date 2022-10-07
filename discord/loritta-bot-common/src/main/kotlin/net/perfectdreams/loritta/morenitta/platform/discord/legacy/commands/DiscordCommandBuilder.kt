package net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands

import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.common.locale.BaseLocale
import dev.kord.common.entity.Permission
import net.perfectdreams.loritta.morenitta.api.commands.CommandBuilder
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext

fun Any?.discordCommand(
	loritta: LorittaBot,
	labels: List<String>,
	category: net.perfectdreams.loritta.common.commands.CommandCategory,
	builder: DiscordCommandBuilder.() -> (Unit)
) = discordCommand(loritta, this?.let { this::class.simpleName } ?: "UnknownCommand", labels, category, builder)

fun discordCommand(
	loritta: LorittaBot,
	commandName: String,
	labels: List<String>,
	category: net.perfectdreams.loritta.common.commands.CommandCategory,
	builder: DiscordCommandBuilder.() -> (Unit)
): DiscordCommand {
	val b = DiscordCommandBuilder(loritta, commandName, labels, category)
	builder.invoke(b)
	return b.buildDiscord()
}

class DiscordCommandBuilder(
	// Needs to be private to avoid accessing this variable on the builder itself
	private val lorittaDiscord: LorittaBot,
	commandName: String,
	labels: List<String>,
	category: net.perfectdreams.loritta.common.commands.CommandCategory
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