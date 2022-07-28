package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.KnuxThrowExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.ManiaTitleCardExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.StudiopolisTvExecutor

class SonicCommand(languageManager: LanguageManager) : CinnamonSlashCommandDeclarationWrapper(languageManager) {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Sonic
    }

    override fun declaration() = slashCommand("sonic", CommandCategory.IMAGES, TodoFixThisData) {
        subcommand(
            "knuxthrow",
            I18N_PREFIX.Knuxthrow
                .Description
        ) {
            executor = { KnuxThrowExecutor(it, it.gabrielaImageServerClient) }
        }

        subcommand("maniatitlecard", I18N_PREFIX.Maniatitlecard.Description) {
            executor = { ManiaTitleCardExecutor(it, it.gabrielaImageServerClient) }
        }

        subcommand("studiopolistv", I18N_PREFIX.Studiopolistv.Description) {
            executor = { StudiopolisTvExecutor(it, it.gabrielaImageServerClient) }
        }
    }
}