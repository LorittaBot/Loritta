package net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft

import net.dv8tion.jda.api.utils.AttachedFile
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ImageReferenceOrAttachment
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.vanilla.minecraft.declaration.MinecraftCommand.Companion.I18N_PREFIX
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.net.URI
import javax.imageio.ImageIO
import kotlin.text.iterator

class MCAchievementExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
    inner class Options : ApplicationCommandOptions() {
        val text = string("text", I18N_PREFIX.Achievement.Options.Text.Description)
        val icon = imageReferenceOrAttachment("icon", I18N_PREFIX.Achievement.Options.Icon.Description)
    }

    override val options = Options()

    override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
        context.deferChannelMessage(false)

        val image= ImageIO.read(URI.create(args[options.icon].get(context)).toURL())
        val text = args[options.text]
        val templatePath = LorittaBot.ASSETS + "/mcconquista.png"
        println(templatePath)
        val templateFile = File(templatePath)

        if (!templateFile.exists())
            error("Template doesn't exist.")

        val template = readImage(templateFile)
        val graphics = template.graphics
        val minecraftia = Constants.MINECRAFTIA
            .deriveFont(24f)

        graphics.font = minecraftia
        graphics.color = Color(255, 255, 0)

        graphics.drawString(context.i18nContext.get(I18N_PREFIX.Achievement.AchievementMade), 90, 41 + 14)
        graphics.color = Color(255, 255, 255)

        var remadeText = ""
        var x = 90
        for (ch in text) {
            if (x + graphics.fontMetrics.charWidth(ch) > 468) {
                remadeText = remadeText.substring(0, remadeText.length - 3) + "..."
                break
            }
            x += graphics.fontMetrics.charWidth(ch)
            remadeText += ch
        }

        graphics.drawString(remadeText, 90, 74 + 14)
        graphics.drawImage(image.getScaledInstance(70, 70, BufferedImage.SCALE_SMOOTH), 16, 14, null)
        graphics.dispose()

        val bArray = template.toByteArray(ImageFormatType.PNG)

        context.reply(false) {
            files.plusAssign(AttachedFile.fromData(bArray, "advancement.png"))
        }
    }

    override suspend fun convertToInteractionsArguments(
        context: LegacyMessageCommandContext,
        args: List<String>
    ): Map<OptionReference<*>, Any?>? {
        val imageUrl = context.imageUrlAt(0) ?: run {
            context.explain()

            return null
        }
        val renewedArgs = context.args.drop(1)

        if (renewedArgs.isEmpty()) {
            context.explain()

            return null
        }

        return mapOf(
            options.icon to ImageReferenceOrAttachment(
                imageUrl,
                context.event.message.attachments.firstOrNull()
            ),
            options.text to renewedArgs.joinToString(" ")
        )
    }
}