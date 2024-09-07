package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base.UnleashedGabrielaImageServerSingleCommandBase
import java.util.*

class WolverineFrameCommand(val client: GabrielaImageServerClient) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Wolverineframe
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.IMAGES, UUID.fromString("cd92b981-f2ae-4970-857d-d312fe6971bb")) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        this.interactionContexts = listOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)

        enableLegacyMessageSupport = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("wolverineframe")
            add("frame")
        }

        executor = WolverineFrameExecutor()
    }

    inner class WolverineFrameExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.wolverineFrame(it) },
        "wolverine_frame.png"
    )
}