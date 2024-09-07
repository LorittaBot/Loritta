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

class ArtCommand(val client: GabrielaImageServerClient) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Art
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.IMAGES, UUID.fromString("f6ef29a6-4b81-42e8-9781-f143e6df8680")) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        this.interactionContexts = listOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)

        enableLegacyMessageSupport = true
        alternativeLegacyAbsoluteCommandPaths.apply {
            add("art")
        }

        executor = ArtCommandExecutor()
    }

    inner class ArtCommandExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.art(it) },
        "art.png"
    )
}