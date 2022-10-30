package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations

import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.DrawnMaskAtendenteExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.DrawnMaskSignExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.DrawnMaskWordExecutor
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.i18n.I18nKeysData

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