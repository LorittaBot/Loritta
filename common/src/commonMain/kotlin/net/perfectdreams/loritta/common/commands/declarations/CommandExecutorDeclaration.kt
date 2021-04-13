package net.perfectdreams.loritta.common.commands.declarations

import net.perfectdreams.loritta.common.commands.options.CommandOptions
import kotlin.reflect.KClass

open class CommandExecutorDeclaration(val parent: KClass<*>) {
    open val options: CommandOptions = CommandOptions.NO_OPTIONS
}