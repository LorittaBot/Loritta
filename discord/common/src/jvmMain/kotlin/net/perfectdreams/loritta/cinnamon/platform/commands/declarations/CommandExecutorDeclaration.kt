package net.perfectdreams.loritta.cinnamon.platform.commands.declarations

import net.perfectdreams.loritta.cinnamon.platform.commands.options.CommandOptions
import kotlin.reflect.KClass

open class CommandExecutorDeclaration(val parent: KClass<*>) {
    open val options: CommandOptions = CommandOptions.NO_OPTIONS
}