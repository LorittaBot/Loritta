package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonMessageCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.ocr.OCRMessageExecutor
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager

class OCRMessageCommand(languageManager: LanguageManager) : CinnamonMessageCommandDeclarationWrapper(languageManager) {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Ocr

    override fun declaration() = messageCommand(I18N_PREFIX.ReadTextFromImage, { OCRMessageExecutor(it) })
}