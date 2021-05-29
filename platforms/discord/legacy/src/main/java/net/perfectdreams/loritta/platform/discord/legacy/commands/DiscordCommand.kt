package net.perfectdreams.loritta.platform.discord.legacy.commands

import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.LorittaUser
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.platform.discord.LorittaDiscord

class DiscordCommand(
	val lorittaDiscord: LorittaDiscord,
	labels: List<String>,
	commandName: String,
	category: CommandCategory,
	descriptionKey: LocaleKeyData = MISSING_DESCRIPTION_KEY,
	description: ((BaseLocale) -> (String)) = {
			it.get(descriptionKey)
		},
	usage: CommandArguments,
	examplesKey: LocaleKeyData?,
	executor: suspend CommandContext.() -> Unit
) : Command<CommandContext>(lorittaDiscord, labels, commandName, category, descriptionKey, description, usage, examplesKey, executor) {
	var userRequiredPermissions = listOf<Permission>()
	var botRequiredPermissions = listOf<Permission>()
	var userRequiredLorittaPermissions = listOf<LorittaPermission>()
	var commandCheckFilter: (suspend (LorittaMessageEvent, List<String>, ServerConfig, BaseLocale, LorittaUser) -> (Boolean))? = null

	override val cooldown: Int
		get() {
			val customCooldown = lorittaDiscord.config.loritta.commands.commandsCooldown[this::class.simpleName]

			if (customCooldown != null)
				return customCooldown

			return if (needsToUploadFiles)
				lorittaDiscord.config.loritta.commands.imageCooldown
			else
				lorittaDiscord.config.loritta.commands.cooldown
		}
}