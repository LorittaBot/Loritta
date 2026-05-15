package net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base

import net.dv8tion.jda.api.utils.AttachedFile
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ImageReferenceOrAttachment
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import java.awt.image.BufferedImage

open class UnleashedLocalSingleImageCommandBase(
    val fileName: String,
    val block: suspend (UnleashedContext, BufferedImage) -> ByteArray
) : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    override val options = UnleashedSingleImageOptions()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false) // Defer message because image manipulation is kinda heavy

        val imageUrl = args[options.imageReference].get(context)

        val image = LorittaUtils.downloadImage(context.loritta, imageUrl) ?: context.fail(false) {
            styled(
                context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound),
                Emotes.LoriSob
            )
        }

        val result = block.invoke(context, image)

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
                options.imageReference to ImageReferenceOrAttachment(
                    firstMention?.effectiveAvatarUrl,
                    context.getImage(0)
                )
            )
        }

        return mapOf(
            options.imageReference to context.getImageReferenceOrAttachment(0)
        )
    }
}
