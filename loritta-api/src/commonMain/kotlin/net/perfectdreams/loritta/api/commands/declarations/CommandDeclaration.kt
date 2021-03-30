package net.perfectdreams.loritta.api.commands.declarations

import net.perfectdreams.loritta.utils.locale.LocaleKeyData

abstract class CommandDeclaration(
    val name: String,
    val description: LocaleKeyData
) {
    open val options: Options = Options.NO_OPTIONS

    abstract class Options {
        companion object {
            val NO_OPTIONS = object: Options() {}
        }

        val arguments = mutableListOf<CommandOption<*>>()
        val subcommands = mutableListOf<CommandDeclaration>()
        val subcommandGroups = mutableListOf<CommandGroupDeclaration>()

        // ===[ SUBCOMMAND ]===
        fun <T : CommandDeclaration> subcommand(declaration: T): T {
            return declaration
        }

        fun <T : CommandDeclaration> T.register(): T {
            subcommands.add(this)
            return this
        }

        // ===[ SUBCOMMAND GROUP ]===
        fun <T : CommandGroupDeclaration> subcommandGroup(groupDeclaration: T): T {
            return groupDeclaration
        }

        fun <T : CommandGroupDeclaration> T.register(): T {
            subcommandGroups.add(this)
            return this
        }

        fun string(name: String, description: LocaleKeyData) = CommandOption<String?>(
            3,
            name,
            description,
            false,
            listOf()
        )

        fun integer(name: String, description: LocaleKeyData) = CommandOption<Int?>(
            4,
            name,
            description,
            false,
            listOf()
        )

        fun boolean(name: String, description: LocaleKeyData) = CommandOption<Boolean?>(
            5,
            name,
            description,
            false,
            listOf()
        )

        fun <T : CommandOption<*>> T.register(): T {
            val duplicate = arguments.any { it.name == this.name }
            if (duplicate)
                throw IllegalArgumentException("Duplicate argument!")

            arguments.add(this)
            return this
        }
    }
}