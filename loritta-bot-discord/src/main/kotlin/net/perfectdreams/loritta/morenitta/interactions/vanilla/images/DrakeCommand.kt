package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base.UnleashedGabrielaImageServerTwoCommandBase

class DrakeCommand(val client: GabrielaImageServerClient) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Drake
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.IMAGES) {
        enableLegacyMessageSupport = true

        subcommand(I18N_PREFIX.Drake.Label, I18N_PREFIX.Drake.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("drake")
            }

            executor = DrakeCommandExecutor()
        }

        subcommand(I18N_PREFIX.Bolsonaro.Label, I18N_PREFIX.Bolsonaro.Description) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("bolsodrake")
            }

            executor = BolsoDrakeCommandExecutor()
        }

        subcommand(I18N_PREFIX.Lori.Label, I18N_PREFIX.Lori.Description) {
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