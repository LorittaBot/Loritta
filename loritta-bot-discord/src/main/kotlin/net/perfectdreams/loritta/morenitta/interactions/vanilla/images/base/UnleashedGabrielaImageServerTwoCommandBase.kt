package net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base

import net.dv8tion.jda.api.utils.AttachedFile
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.data.TwoImagesRequest
import net.perfectdreams.gabrielaimageserver.data.URLImageData
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver.handleExceptions
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ImageReference
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference

open class UnleashedGabrielaImageServerTwoCommandBase(
    val client: GabrielaImageServerClient,
    val block: suspend GabrielaImageServerClient.(TwoImagesRequest) -> (ByteArray),
    val fileName: String
) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    override val options = UnleashedTwoImageOptions()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false)

        val imageReference1 = args[options.imageReference1].get(context)
        val imageReference2 = args[options.imageReference2].get(context)

        val result = client.handleExceptions(context) {
            block.invoke(
                client,
                TwoImagesRequest(
                    URLImageData(
                        imageReference1
                    ),
                    URLImageData(
                        imageReference2
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

        val data2 = args.getOrNull(1)
        val secondMention = context.mentions.users.getOrNull(1)

        if (data == firstMention?.asMention && data2 == secondMention?.asMention) {
            return mapOf(
                options.imageReference1 to ImageReference(
                    firstMention?.effectiveAvatarUrl,
                    context.getImage(0)
                ),
                options.imageReference2 to ImageReference(
                    secondMention?.effectiveAvatarUrl,
                    context.getImage(1)
                )
            )
        }

        return mapOf(
            options.imageReference1 to ImageReference(
                data,
                context.getImage(0)
            ),
            options.imageReference2 to ImageReference(
                data2,
                context.getImage(1)
            )
        )
    }
}