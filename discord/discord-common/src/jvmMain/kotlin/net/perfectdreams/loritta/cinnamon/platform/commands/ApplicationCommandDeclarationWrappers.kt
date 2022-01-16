package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

interface SlashCommandDeclarationWrapper {
    fun declaration(): SlashCommandDeclarationBuilder

    fun slashCommand(labels: List<String>, category: CommandCategory, description: StringI18nData, block: SlashCommandDeclarationBuilder.() -> (Unit))
            = slashCommand(this::class, labels, category, description, block)
}