package net.perfectdreams.loritta.cinnamon.discord.commands.declarations

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.commands.CommandCategory

interface CommandDeclaration {
    fun declaration(): CommandDeclarationBuilder

    fun command(labels: List<String>, category: CommandCategory, description: StringI18nData, block: CommandDeclarationBuilder.() -> (Unit))
            = command(this::class, labels, category, description, block)
}