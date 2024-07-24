package net.perfectdreams.loritta.morenitta.interactions.vanilla.utils

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.commands.Command
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.*
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ImageReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference

class OCRSlashCommand(val loritta: LorittaBot) : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand(OCRExecutor.I18N_PREFIX.Label, OCRExecutor.I18N_PREFIX.Description, CommandCategory.UTILS) {
        enableLegacyMessageSupport = true

        alternativeLegacyLabels.apply {
            add("ler")
            add("read")
        }

        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)

        executor = OCRSlashExecutor()
    }

    inner class OCRSlashExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        inner class Options : ApplicationCommandOptions() {
            val imageReference = imageReferenceOrAttachment("image", OCRExecutor.I18N_PREFIX.Label)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            val imageReference = args[options.imageReference]
            val imageUrl = imageReference.get(context)

            OCRExecutor.handleOCRCommand(
                loritta,
                context,
                false,
                imageUrl
            )
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return mapOf(
                options.imageReference to ImageReference(
                    context.args.firstOrNull(),
                    context.event.message.attachments.firstOrNull()
                )
            )
        }
    }
}