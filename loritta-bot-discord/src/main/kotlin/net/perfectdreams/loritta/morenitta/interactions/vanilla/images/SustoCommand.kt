package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base.UnleashedGabrielaImageServerSingleCommandBase

class SustoCommand(val client: GabrielaImageServerClient) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Fright
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.IMAGES) {
        enableLegacyMessageSupport = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("fright")
            add("susto")
            add("lorisusto")
        }

        executor = SustoExecutor()
    }

    inner class SustoExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.loriScared(it) },
        "loritta_susto.png"
    )
}