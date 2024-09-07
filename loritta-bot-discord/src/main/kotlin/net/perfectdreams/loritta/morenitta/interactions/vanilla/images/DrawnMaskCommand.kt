package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import net.dv8tion.jda.api.utils.AttachedFile
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.DrawnMaskAtendenteRequest
import net.perfectdreams.gabrielaimageserver.data.DrawnMaskWordRequest
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base.UnleashedGabrielaImageServerSingleCommandBase
import java.util.*

class DrawnMaskCommand(val client: GabrielaImageServerClient) : SlashCommandDeclarationWrapper {
    companion object {
        val I18N_PREFIX = I18nKeysData.Commands.Command.Drawnmask
    }

    override fun command() = slashCommand(I18N_PREFIX.Label, I18N_PREFIX.Description, CommandCategory.IMAGES, UUID.fromString("6dc0e9be-83c5-4d67-81f1-e0cc57eae08f")) {
        enableLegacyMessageSupport = true

        subcommand(I18N_PREFIX.Atendente.Label, I18N_PREFIX.Atendente.Description, UUID.fromString("dc029079-399e-4e60-88bc-f747dbce858d")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("atendente")
            }

            executor = DrawnMaskAtendenteExecutor()
        }

        subcommand(I18N_PREFIX.Sign.Label, I18N_PREFIX.Sign.Description, UUID.fromString("f83f6cc0-56d2-47d3-8126-e2255f4bf56c")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("drawnmasksign")
            }

            executor = DrawnMaskSignExecutor()
        }

        subcommand(I18N_PREFIX.Word.Label, I18N_PREFIX.Word.Description, UUID.fromString("37ee0692-075e-4275-850b-f7057e820d0e")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("drawnmaskword")
                add("drawnword")
            }

            executor = DrawnMaskWordExecutor()
        }
    }

    inner class DrawnMaskAtendenteExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val text = string("text", SonicCommand.I18N_PREFIX.Maniatitlecard.Options.Line1)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val text = args[options.text]

            val result = client.handleExceptions(context) {
                client.images.drawnMaskAtendente(
                    DrawnMaskAtendenteRequest(text)
                )
            }

            context.reply(false) {
                files.plusAssign(
                    AttachedFile.fromData(
                        result,
                        "atendente.png"
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(
                options.text to args.joinToString(" ")
            )
        }
    }

    inner class DrawnMaskSignExecutor : UnleashedGabrielaImageServerSingleCommandBase(
        client,
        { client.images.drawnMaskSign(it) },
        "drawn_mask_sign.png"
    )

    inner class DrawnMaskWordExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val text = string("text", SonicCommand.I18N_PREFIX.Maniatitlecard.Options.Line1)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val text = args[options.text]

            val result = client.handleExceptions(context) {
                client.images.drawnMaskWord(
                    DrawnMaskWordRequest(text)
                )
            }

            context.reply(false) {
                files.plusAssign(
                    AttachedFile.fromData(
                        result,
                        "drawn_mask_word.png"
                    )
                )
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            if (args.isEmpty()) {
                context.explain()
                return null
            }

            return mapOf(
                options.text to args.joinToString(" ")
            )
        }
    }
}