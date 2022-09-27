package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.BolsoDrakeExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.DrakeExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.LoriDrakeExecutor

class DrakeCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Drake
    }

    override fun declaration() = slashCommand(I18N_PREFIX.Drake.Label, CommandCategory.IMAGES, I18N_PREFIX.Description) {
        subcommand(I18N_PREFIX.Drake.Label, I18N_PREFIX.Drake.Description) {
            executor = { DrakeExecutor(it, it.gabrielaImageServerClient) }
        }

        subcommand(I18N_PREFIX.Bolsonaro.Label, I18N_PREFIX.Bolsonaro.Description) {
            executor = { BolsoDrakeExecutor(it, it.gabrielaImageServerClient) }
        }

        subcommand(I18N_PREFIX.Lori.Label, I18N_PREFIX.Lori.Description) {
            executor = { LoriDrakeExecutor(it, it.gabrielaImageServerClient) }
        }
    }
}