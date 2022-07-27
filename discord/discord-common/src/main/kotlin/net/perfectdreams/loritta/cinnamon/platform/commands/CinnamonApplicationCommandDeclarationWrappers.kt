package net.perfectdreams.loritta.cinnamon.platform.commands

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon

interface CinnamonApplicationCommandDeclarationWrapper

abstract class CinnamonSlashCommandDeclarationWrapper(val loritta: LorittaCinnamon) : CinnamonApplicationCommandDeclarationWrapper {
    abstract fun declaration(): LorittaSlashCommandDeclarationBuilder

    fun slashCommand(label: String, category: CommandCategory, description: StringI18nData, block: LorittaSlashCommandDeclarationBuilder.() -> (Unit))
            = slashCommand(loritta.languageManager, label, description, category, block)
}

abstract class CinnamonUserCommandDeclarationWrapper(val loritta: LorittaCinnamon) : CinnamonApplicationCommandDeclarationWrapper {
    abstract fun declaration(): LorittaUserCommandDeclarationBuilder

    fun userCommand(name: StringI18nData, executor: CinnamonUserCommandExecutor, block: LorittaUserCommandDeclarationBuilder.() -> (Unit) = {})
            = userCommand(loritta.languageManager, name, executor, block)
}