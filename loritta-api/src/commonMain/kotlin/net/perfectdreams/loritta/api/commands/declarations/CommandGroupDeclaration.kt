package net.perfectdreams.loritta.api.commands.declarations

import net.perfectdreams.loritta.utils.locale.LocaleKeyData

abstract class CommandGroupDeclaration(
    val name: String,
    val description: LocaleKeyData
) {
    val subcommands = mutableListOf<CommandDeclaration>()

    fun <T : CommandDeclaration> subcommand(declaration: T): T {
        return declaration
    }

    fun <T : CommandDeclaration> T.register(): T {
        subcommands.add(this)
        return this
    }
}