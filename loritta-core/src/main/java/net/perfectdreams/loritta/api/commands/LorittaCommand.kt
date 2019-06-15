package net.perfectdreams.loritta.api.commands

import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import mu.KotlinLogging
import net.perfectdreams.commands.Command
import net.perfectdreams.loritta.api.platform.LorittaBot
import net.perfectdreams.loritta.api.platform.PlatformFeature
import net.perfectdreams.loritta.utils.Emotes
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

open class LorittaCommand(override val labels: Array<String>, val category: CommandCategory) : Command() {
	lateinit var loritta: LorittaBot

	internal val logger = KotlinLogging.logger {}
	open val requiresFeatures = listOf<PlatformFeature>()
	open val onlyOwner: Boolean = false

	open val cooldown: Int
		get() {
			val customCooldown = loritta.config.loritta.commands.commandsCooldown[this::class.simpleName]

			if (customCooldown != null)
				return customCooldown

			return if (needsToUploadFiles)
				loritta.config.loritta.commands.imageCooldown
			else
				loritta.config.loritta.commands.cooldown
		}

	open var executedCount: Int = 0
	open val hasCommandFeedback: Boolean = true
	open val lorittaPermissions = listOf<LorittaPermission>()
	open val canUseInPrivateChannel: Boolean = true
	open val requiresMusic: Boolean = false
	open val needsToUploadFiles = false

	open fun getDescription(locale: BaseLocale): String? {
		return null
	}

	open fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {}
	}

	open fun getExamples(locale: BaseLocale): List<String> {
		return listOf()
	}
}

@ExperimentalContracts
fun <T> LorittaCommand.notNull(value: T?, message: String, prefix: String = Constants.ERROR): T { // Contracts precisam ser fora de uma classe, então...
	contract {
		returns() implies (value != null)
	}

	if(value == null)
		throw CommandException(message, prefix)

	return value
}

@ExperimentalContracts
fun <T> LorittaCommand.notNullImage(value: T?, context: LorittaCommandContext, prefix: String = Constants.ERROR) = notNull(value, context.locale["commands.noValidImageFound", Emotes.LORI_CRYING.toString()], prefix)