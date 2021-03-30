package net.perfectdreams.loritta.api.commands.declarations

abstract class CommandGroupDeclaration(
    val name: String,
    val description: String
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