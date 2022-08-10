package net.perfectdreams.loritta.cinnamon.discord.interactions.commands

import net.perfectdreams.discordinteraktions.common.commands.UserCommandExecutor
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon

interface CinnamonApplicationCommandDeclarationWrapper

abstract class CinnamonSlashCommandDeclarationWrapper(val languageManager: LanguageManager) : CinnamonApplicationCommandDeclarationWrapper {
    abstract fun declaration(): CinnamonSlashCommandDeclarationBuilder

    fun slashCommand(label: String, category: CommandCategory, description: StringI18nData, block: CinnamonSlashCommandDeclarationBuilder.() -> (Unit))
            = slashCommand(this, languageManager, label, description, category, block)
}

abstract class CinnamonUserCommandDeclarationWrapper(val languageManager: LanguageManager) : CinnamonApplicationCommandDeclarationWrapper {
    abstract fun declaration(): CinnamonUserCommandDeclarationBuilder

    fun userCommand(name: StringI18nData, executor: (LorittaCinnamon) -> (UserCommandExecutor), block: CinnamonUserCommandDeclarationBuilder.() -> (Unit) = {})
            = userCommand(this, languageManager, name, executor, block)
}