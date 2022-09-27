package net.perfectdreams.loritta.cinnamon.discord.interactions.commands

import net.perfectdreams.discordinteraktions.common.commands.MessageCommandExecutor
import net.perfectdreams.discordinteraktions.common.commands.UserCommandExecutor
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.common.commands.CommandCategory

interface CinnamonApplicationCommandDeclarationWrapper

abstract class CinnamonSlashCommandDeclarationWrapper(val languageManager: LanguageManager) : CinnamonApplicationCommandDeclarationWrapper {
    abstract fun declaration(): CinnamonSlashCommandDeclarationBuilder

    fun slashCommand(label: StringI18nData, category: CommandCategory, description: StringI18nData, block: CinnamonSlashCommandDeclarationBuilder.() -> (Unit))
            = slashCommand(this, languageManager, label, description, category, block)
}

abstract class CinnamonUserCommandDeclarationWrapper(val languageManager: LanguageManager) : CinnamonApplicationCommandDeclarationWrapper {
    abstract fun declaration(): CinnamonUserCommandDeclarationBuilder

    fun userCommand(name: StringI18nData, executor: (LorittaBot) -> (UserCommandExecutor), block: CinnamonUserCommandDeclarationBuilder.() -> (Unit) = {})
            = userCommand(this, languageManager, name, executor, block)
}

abstract class CinnamonMessageCommandDeclarationWrapper(val languageManager: LanguageManager) : CinnamonApplicationCommandDeclarationWrapper {
    abstract fun declaration(): CinnamonMessageCommandDeclarationBuilder

    fun messageCommand(name: StringI18nData, executor: (LorittaBot) -> (MessageCommandExecutor), block: CinnamonMessageCommandDeclarationBuilder.() -> (Unit) = {})
            = messageCommand(this, languageManager, name, executor, block)
}