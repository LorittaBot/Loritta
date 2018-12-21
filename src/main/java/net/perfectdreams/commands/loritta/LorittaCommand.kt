package net.perfectdreams.commands.loritta

import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.commands.Command
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

open class LorittaCommand(override val labels: Array<String>) : Command() {

}

@ExperimentalContracts
fun <T> LorittaCommand.notNull(value: T?, message: String, prefix: String = Constants.ERROR) { // Contracts precisam ser fora de uma classe, então...
	contract {
		returns() implies (value != null)
	}
	if(value == null) throw CommandException(message, prefix)
}