package net.perfectdreams.loritta.cinnamon.platform.commands.images.declarations

import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.platform.commands.images.DrawnMaskAtendenteExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.DrawnMaskSignExecutor
import net.perfectdreams.loritta.cinnamon.platform.commands.images.DrawnMaskWordExecutor

object DrawnMaskCommand : SlashCommandDeclarationWrapper {
    val I18N_PREFIX = I18nKeysData.Commands.Command.Drawnmask

    override fun declaration() = slashCommand(listOf("drawnmask"), CommandCategory.IMAGES, I18N_PREFIX.Description) {
        subcommand(listOf("atendente"), I18N_PREFIX.Atendente.Description) {
            executor = DrawnMaskAtendenteExecutor
        }

        subcommand(listOf("sign"), I18N_PREFIX.Sign.Description) {
            executor = DrawnMaskSignExecutor
        }

        subcommand(listOf("word"), I18N_PREFIX.Word.Description) {
            executor = DrawnMaskWordExecutor
        }
    }
}