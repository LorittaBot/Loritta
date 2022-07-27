package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.images.DrawnMaskAtendenteExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.DrawnMaskSignExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.DrawnMaskWordExecutor

class DrawnMaskCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Drawnmask
    }

    override fun declaration() = slashCommand("drawnmask", CommandCategory.IMAGES, I18N_PREFIX.Description) {
        subcommand("atendente", I18N_PREFIX.Atendente.Description) {
            executor = { DrawnMaskAtendenteExecutor(it, it.gabrielaImageServerClient) }
        }

        subcommand("sign", I18N_PREFIX.Sign.Description) {
            executor = { DrawnMaskSignExecutor(it, it.gabrielaImageServerClient) }
        }

        subcommand("word", I18N_PREFIX.Word.Description) {
            executor = { DrawnMaskWordExecutor(it, it.gabrielaImageServerClient) }
        }
    }
}