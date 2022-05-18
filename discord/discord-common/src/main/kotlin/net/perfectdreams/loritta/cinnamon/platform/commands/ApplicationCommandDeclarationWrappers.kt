package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

interface ApplicationCommandDeclarationWrapper {
    fun declaration(): ApplicationCommandDeclaration
}

interface SlashCommandDeclarationWrapper : ApplicationCommandDeclarationWrapper {
    override fun declaration(): SlashCommandDeclaration

    fun slashCommand(labels: List<String>, category: CommandCategory, description: StringI18nData, block: SlashCommandDeclarationBuilder.() -> (Unit))
            = slashCommand(labels, description, category, block)
}

interface UserCommandDeclarationWrapper : ApplicationCommandDeclarationWrapper {
    override fun declaration(): UserCommandDeclaration
}
