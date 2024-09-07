package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base.UnleashedGabrielaImageServerTwoCommandBase
import java.util.*

class DrakeCommand(val client: GabrielaImageServerClient) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Drake
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.IMAGES, UUID.fromString("c51567dd-5d35-4f42-be93-13faf52e66f1")) {
        enableLegacyMessageSupport = true

        subcommand(I18N_PREFIX.Drake.Label, I18N_PREFIX.Drake.Description, UUID.fromString("2b858d29-10de-4153-9228-1ed4b25072e7")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("drake")
            }

            executor = DrakeCommandExecutor()
        }

        subcommand(I18N_PREFIX.Bolsonaro.Label, I18N_PREFIX.Bolsonaro.Description, UUID.fromString("44673256-508f-423b-be2c-5bde7c008105")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("bolsodrake")
            }

            executor = BolsoDrakeCommandExecutor()
        }

        subcommand(I18N_PREFIX.Lori.Label, I18N_PREFIX.Lori.Description, UUID.fromString("3ef10dca-f48b-4806-9346-ee46dab2c027")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("loridrake")
            }

            executor = LoriDrakeCommandExecutor()
        }
    }

    inner class DrakeCommandExecutor : UnleashedGabrielaImageServerTwoCommandBase(
        client,
        { client.images.drake(it) },
        "drake.png"
    )

    inner class BolsoDrakeCommandExecutor : UnleashedGabrielaImageServerTwoCommandBase(
        client,
        { client.images.bolsoDrake(it) },
        "bolsodrake.png"
    )

    inner class LoriDrakeCommandExecutor : UnleashedGabrielaImageServerTwoCommandBase(
        client,
        { client.images.loriDrake(it) },
        "lori_drake.png"
    )
}