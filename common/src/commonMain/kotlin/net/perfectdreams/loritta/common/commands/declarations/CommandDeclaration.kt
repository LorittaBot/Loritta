package net.perfectdreams.loritta.common.commands.declarations

import net.perfectdreams.loritta.common.commands.CommandCategory

interface CommandDeclaration {
    fun declaration(): CommandDeclarationBuilder

    fun command(labels: List<String>, category: CommandCategory, block: CommandDeclarationBuilder.() -> (Unit))
            = command(this::class, labels, category, block)
}