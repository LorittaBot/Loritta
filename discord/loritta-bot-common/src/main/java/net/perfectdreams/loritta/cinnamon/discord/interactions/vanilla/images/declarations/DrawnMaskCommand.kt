package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.DrawnMaskAtendenteExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.DrawnMaskSignExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.DrawnMaskWordExecutor

class DrawnMaskCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Drawnmask
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Label, CommandCategory.IMAGES, I18N_PREFIX.Description) {
        subcommand(I18N_PREFIX.Atendente.Label, I18N_PREFIX.Atendente.Description) {
            executor = { DrawnMaskAtendenteExecutor(it, it.gabrielaImageServerClient) }
        }

        subcommand(I18N_PREFIX.Sign.Label, I18N_PREFIX.Sign.Description) {
            executor = { DrawnMaskSignExecutor(it, it.gabrielaImageServerClient) }
        }

        subcommand(I18N_PREFIX.Word.Label, I18N_PREFIX.Word.Description) {
            executor = { DrawnMaskWordExecutor(it, it.gabrielaImageServerClient) }
        }
    }
}