package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.colorinfo.DecimalColorInfoExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.colorinfo.HexColorInfoExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.colorinfo.RgbColorInfoExecutor

class ColorInfoCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Colorinfo
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.UTILS, I18N_PREFIX.Description) {
        subcommand(I18N_PREFIX.RgbColorInfo.Label, I18N_PREFIX.RgbColorInfo.Description) {
            executor = { RgbColorInfoExecutor(it) }
        }

        subcommand(I18N_PREFIX.HexColorInfo.Label, I18N_PREFIX.HexColorInfo.Description) {
            executor = { HexColorInfoExecutor(it) }
        }

        subcommand(I18N_PREFIX.DecimalColorInfo.Label, I18N_PREFIX.DecimalColorInfo.Description) {
            executor = { DecimalColorInfoExecutor(it) }
        }
    }
}