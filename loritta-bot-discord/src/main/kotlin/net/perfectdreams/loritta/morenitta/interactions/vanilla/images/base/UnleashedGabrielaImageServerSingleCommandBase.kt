package net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base

import net.dv8tion.jda.api.utils.AttachedFile
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.SingleImageRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ImageReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference

open class UnleashedGabrielaImageServerSingleCommandBase(
    val client: GabrielaImageServerClient,
    val block: suspend GabrielaImageServerClient.(SingleImageRequest) -> (ByteArray),
    val fileName: String
) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    override val options = UnleashedSingleImageOptions()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false) // Defer message because image manipulation is kinda heavy

        val imageReference = args[options.imageReference].get(context)

        val result = client.handleExceptions(context) {
            block.invoke(
                client,
                SingleImageRequest(
                    URLImageData(
                        imageReference
                    )
                )
            )
        }

        context.reply(false) {
            files.plusAssign(
                AttachedFile.fromData(
                    result,
                    fileName
                )
            )
        }
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?> {
        val data = args.getOrNull(0)
        val firstMention = context.mentions.users.firstOrNull()

        if (data == firstMention?.asMention) {
            return mapOf(
                options.imageReference to ImageReference(
                    firstMention?.effectiveAvatarUrl,
                    context.getImage(0)
                )
            )
        }

        return mapOf(
            options.imageReference to ImageReference(
                data,
                context.getImage(0)
            )
        )
    }
}