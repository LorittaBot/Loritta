package net.perfectdreams.loritta.cinnamon.platform.commands.utils.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.colorinfo.DecimalColorInfoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.colorinfo.HexColorInfoExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.utils.colorinfo.RgbColorInfoExecutor

class ColorInfoCommand(loritta: LorittaCinnamon, val gabiClient: GabrielaImageServerClient) : CinnamonSlashCommandDeclarationWrapper(loritta) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Colorinfo
    }

    override fun declaration() = slashCommand("colorinfo", CommandCategory.UTILS, I18N_PREFIX.Description) {
        subcommand("rgb", I18N_PREFIX.RgbColorInfo.Description) {
            executor = RgbColorInfoExecutor(loritta, gabiClient)
        }

        subcommand("hex", I18N_PREFIX.HexColorInfo.Description) {
            executor = HexColorInfoExecutor(loritta, gabiClient)
        }

        subcommand("decimal", I18N_PREFIX.DecimalColorInfo.Description) {
            executor = DecimalColorInfoExecutor(loritta, gabiClient)
        }
    }
}