package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.utils.AttachedFile
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.LorittaImage
import net.perfectdreams.loritta.common.utils.TodoFixThisData
import net.perfectdreams.loritta.common.utils.extensions.enableFontAntiAliasing
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.gifs.DemonGIF
import net.perfectdreams.loritta.morenitta.gifs.GifSequenceWriter
import net.perfectdreams.loritta.morenitta.gifs.GumballGIF
import net.perfectdreams.loritta.morenitta.gifs.SwingGIF
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LegacyMessageCommandContext
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaLegacyMessageCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.LorittaSlashCommandExecutor
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandArguments
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ApplicationCommandOptions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.ImageReferenceOrAttachment
import net.perfectdreams.loritta.morenitta.interactions.commands.options.OptionReference
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base.UnleashedLocalSingleImageCommandBase
import net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base.UnleashedLocalSingleImageGifCommandBase
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.SeamCarver
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.stream.FileImageOutputStream
import kotlin.math.max

class MemeCommand2 : SlashCommandDeclarationWrapper {
    override fun command() = slashCommand(I18nKeysData.Commands.Command.Meme2.Label, I18nKeysData.Commands.Command.Meme2.Description, CommandCategory.IMAGES, UUID.fromString("e2a68cc3-de4e-4dc9-b3d4-46f7cfde1c20")) {
        this.integrationTypes = listOf(IntegrationType.GUILD_INSTALL, IntegrationType.USER_INSTALL)
        this.interactionContexts = listOf(InteractionContextType.GUILD, InteractionContextType.BOT_DM, InteractionContextType.PRIVATE_CHANNEL)

        this.enableLegacyMessageSupport = true

        subcommand(I18nKeysData.Commands.Command.Dieplague.Label, I18nKeysData.Commands.Command.Dieplague.Description, UUID.fromString("ea1e6282-122e-4dcf-9987-4684ed29bf3b")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("dieplague")
                add("morrepraga")
            }

            executor = DiePlagueExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Forgive.Label, I18nKeysData.Commands.Command.Forgive.Description, UUID.fromString("622f5b6c-a049-4bc9-a34a-248db453f0f2")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("perdao")
                add("perdão")
            }

            executor = ForgiveExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.God.Label, I18nKeysData.Commands.Command.God.Description, UUID.fromString("8eb423c4-bd35-48f8-9a59-d839ff49cade")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("god")
                add("deus")
            }

            executor = GodExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Jooj.Label, I18nKeysData.Commands.Command.Jooj.Description, UUID.fromString("6d7c50e2-f901-422e-9f49-cc56bc4176b8")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("jooj")
            }

            executor = JoojExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Ojjo.Label, I18nKeysData.Commands.Command.Ojjo.Description, UUID.fromString("62d6f6a2-edbc-4098-a908-61dd4e186fe7")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("ojjo")
            }

            executor = OjjoExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Lava.Label, I18nKeysData.Commands.Command.Lava.Description, UUID.fromString("9e9de728-64b4-4ea5-8c9b-461a76213eaf")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("lava")
            }

            executor = LavaExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Lavareverse.Label, I18nKeysData.Commands.Command.Lavareverse.Description, UUID.fromString("edbe6d45-db98-430e-8a17-6c9f48f7f977")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("lavareverse")
                add("lavareverso")
                add("reverselava")
            }

            executor = LavaReverseExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Laranjo.Label, I18nKeysData.Commands.Command.Laranjo.Description, UUID.fromString("82cbf701-a89e-4b6c-a186-6194e961c06d")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("laranjo")
            }

            executor = LaranjoExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Gumball.Label, I18nKeysData.Commands.Command.Gumball.Description, UUID.fromString("252207b0-e499-446a-ab07-732f72458fc1")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("gumball")
            }

            executor = GumballExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Demon.Label, I18nKeysData.Commands.Command.Demon.Description, UUID.fromString("26aea47b-b60a-4767-a4fa-fadb7f99c314")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("demon")
                add("demonio")
                add("demônio")
                add("demónio")
            }

            executor = DemonExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Triggered.Label, I18nKeysData.Commands.Command.Triggered.Description, UUID.fromString("0b307460-56b2-4f36-87cc-92503c0b9dd8")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("triggered")
            }

            executor = TriggeredExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Swing.Label, I18nKeysData.Commands.Command.Swing.Description, UUID.fromString("ed5c99b8-cbac-4a83-b45e-94502bbd35d3")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("swing")
            }

            executor = SwingExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Contentawarescale.Label, I18nKeysData.Commands.Command.Contentawarescale.Description, UUID.fromString("8c220dc3-a28f-42bd-b8d0-76bdbeaec2d6")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("contentawarescale")
                add("cas")
                add("contentaware")
                add("seamcarver")
            }

            executor = ContentAwareScaleExecutor()
        }
    }

    class DiePlagueExecutor : UnleashedLocalSingleImageCommandBase(
        "morre_praga.png",
        { context, contextImage ->
            val template = (context.loritta.assets.loadImage("morre_praga.png", loadFromCache = false) as JVMImage).handle as BufferedImage
            val graphics = template.createGraphics()
            val scaled = contextImage.getScaledInstance(312, 312, BufferedImage.SCALE_SMOOTH)

            graphics.enableFontAntiAliasing()
            val cambriaItalic = Font.createFont(Font.TRUETYPE_FONT, File(LorittaBot.ASSETS, "fonts/Cambria-Italic.ttf"))
            graphics.color = Color.BLACK
            graphics.font = cambriaItalic.deriveFont(150f)

            ImageUtils.drawCenteredString(
                graphics,
                context.i18nContext.get(I18nKeysData.Commands.Command.Dieplague.TopText),
                Rectangle(25, 38, 502, 132),
                graphics.font
            )

            ImageUtils.drawCenteredString(
                graphics,
                context.i18nContext.get(I18nKeysData.Commands.Command.Dieplague.BottomText),
                Rectangle(43, 480, 468, 139),
                graphics.font
            )

            graphics.drawImage(scaled, 115, 183, null)

            template.toByteArray(ImageFormatType.PNG)
        }
    )

    class ForgiveExecutor : UnleashedLocalSingleImageCommandBase(
        "perdao.png",
        { context, contextImage ->
            val template = (context.loritta.assets.loadImage("perdao.png", loadFromCache = false) as JVMImage).handle as BufferedImage

            val newHeight = (contextImage.width * template.height) / template.width
            val scaledTemplate = template.getScaledInstance(contextImage.width, max(newHeight, 1), BufferedImage.SCALE_SMOOTH)
            contextImage.graphics.drawImage(scaledTemplate, 0, contextImage.height - scaledTemplate.getHeight(null), null)

            contextImage.toByteArray(ImageFormatType.PNG)
        }
    )

    class GodExecutor : UnleashedLocalSingleImageCommandBase(
        "deus.png",
        { context, contextImage ->
            val template = (context.loritta.assets.loadImage("deus.png", loadFromCache = false) as JVMImage).handle as BufferedImage

            val scaled = contextImage.getScaledInstance(87, 87, BufferedImage.SCALE_SMOOTH)
            template.graphics.drawImage(scaled, 1, 1, null)

            template.toByteArray(ImageFormatType.PNG)
        }
    )

    class JoojExecutor : UnleashedLocalSingleImageCommandBase(
        "jooj.png",
        { _, contextImage ->
            // We need to create an empty "base" to avoid issues with transparent images
            val baseImage = BufferedImage(contextImage.width, contextImage.height, BufferedImage.TYPE_INT_ARGB)

            val leftSide = contextImage.getSubimage(0, 0, contextImage.width / 2, contextImage.height)

            val tx = AffineTransform.getScaleInstance(-1.0, 1.0)
            tx.translate(-leftSide.getWidth(null).toDouble(), 0.0)
            val op = AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
            val leftSideFlipped = op.filter(leftSide, null)

            baseImage.graphics.drawImage(leftSide, 0, 0, null)
            baseImage.graphics.drawImage(leftSideFlipped, baseImage.width / 2, 0, null)

            baseImage.toByteArray(ImageFormatType.PNG)
        }
    )

    class OjjoExecutor : UnleashedLocalSingleImageCommandBase(
        "ojjo.png",
        { _, contextImage ->
            val baseImage = BufferedImage(contextImage.width, contextImage.height, BufferedImage.TYPE_INT_ARGB)

            val rightSide = contextImage.getSubimage(contextImage.width / 2, 0, contextImage.width / 2, contextImage.height)

            val tx = AffineTransform.getScaleInstance(-1.0, 1.0)
            tx.translate(-rightSide.getWidth(null).toDouble(), 0.0)
            val op = AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
            val rightSideFlipped = op.filter(rightSide, null)

            baseImage.graphics.drawImage(rightSideFlipped, 0, 0, null)
            baseImage.graphics.drawImage(rightSide, baseImage.width / 2, 0, null)

            baseImage.toByteArray(ImageFormatType.PNG)
        }
    )

    class LavaExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val text = string("text", I18nKeysData.Commands.Command.Lava.Options.Text.Text)
            val imageReference = imageReferenceOrAttachment("image", TodoFixThisData)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val imageUrl = args[options.imageReference].get(context)
            val text = args[options.text]
            val contextImage = LorittaUtils.downloadImage(context.loritta, imageUrl) ?: context.fail(false) {
                styled(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
            }

            val template = (context.loritta.assets.loadImage("lava.png", loadFromCache = false) as JVMImage).handle as BufferedImage

            val resized = contextImage.getScaledInstance(64, 64, BufferedImage.SCALE_SMOOTH)
            val small = contextImage.getScaledInstance(32, 32, BufferedImage.SCALE_SMOOTH)
            val templateGraphics = template.graphics
            templateGraphics.drawImage(resized, 120, 0, null)
            templateGraphics.drawImage(small, 487, 0, null)

            val canvas = BufferedImage(700, 443, BufferedImage.TYPE_INT_ARGB)
            val graphics = canvas.createGraphics().apply { enableFontAntiAliasing() }
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, 700, 443)
            graphics.color = Color.BLACK
            graphics.drawImage(template, 0, 100, null)

            val font = Font.createFont(Font.TRUETYPE_FONT, File(LorittaBot.ASSETS, "mavenpro-bold.ttf")).deriveFont(24F)
            graphics.font = font

            val isPlural = text.split(" ").firstOrNull()?.endsWith("s", true) == true
            val floor = context.i18nContext.get(
                I18nKeysData.Commands.Command.Lava.Floor(
                    kind = if (isPlural) "plural" else "singular",
                    what = text
                )
            )
            ImageUtils.drawCenteredString(graphics, floor, Rectangle(2, 2, 700, 100), font)

            val result = canvas.toByteArray(ImageFormatType.PNG)
            context.reply(false) {
                files += AttachedFile.fromData(result, "lava.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val text = args.drop(1).joinToString(" ")
            if (text.isBlank()) {
                context.explain()
                return null
            }

            return mapOf(
                options.imageReference to ImageReferenceOrAttachment(args.getOrNull(0), context.getImage(0)),
                options.text to text
            )
        }
    }

    class LavaReverseExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val text = string("text", I18nKeysData.Commands.Command.Lavareverse.Options.Text.Text)
            val imageReference = imageReferenceOrAttachment("image", TodoFixThisData)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val imageUrl = args[options.imageReference].get(context)
            val text = args[options.text]
            val contextImage = LorittaUtils.downloadImage(context.loritta, imageUrl) ?: context.fail(false) {
                styled(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
            }

            val template = (context.loritta.assets.loadImage("lavareverso.png", loadFromCache = false) as JVMImage).handle as BufferedImage

            // Resize first so the rotation doesn't look pixelated
            val firstImage = contextImage.getScaledInstance(256, 256, BufferedImage.SCALE_SMOOTH)
            // Expand the canvas so the rotation has room to breathe
            val firstImageCanvas = BufferedImage(326, 326, BufferedImage.TYPE_INT_ARGB)
            firstImageCanvas.graphics.drawImage(firstImage, 35, 35, null)

            val transform = AffineTransform()
            transform.rotate(0.436332, (firstImageCanvas.width / 2).toDouble(), (firstImageCanvas.height / 2).toDouble())
            val op = AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR)
            val rotated = op.filter(firstImageCanvas, null)

            val resized = rotated.getScaledInstance(196, 196, BufferedImage.SCALE_SMOOTH)
            val small = contextImage.getScaledInstance(111, 111, BufferedImage.SCALE_SMOOTH)
            val templateGraphics = template.graphics
            templateGraphics.drawImage(resized, 100, 0, null)
            templateGraphics.drawImage(small, 464, 175, null)

            val canvas = BufferedImage(693, 766, BufferedImage.TYPE_INT_ARGB)
            val graphics = canvas.createGraphics().apply { enableFontAntiAliasing() }
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, 693, 766)
            graphics.color = Color.BLACK
            graphics.drawImage(template, 0, 100, null)

            val font = Font.createFont(Font.TRUETYPE_FONT, File(LorittaBot.ASSETS, "mavenpro-bold.ttf")).deriveFont(32F)
            graphics.font = font

            val isPlural = text.split(" ").firstOrNull()?.endsWith("s", true) == true
            val floor = context.i18nContext.get(
                I18nKeysData.Commands.Command.Lava.Floor(
                    kind = if (isPlural) "plural" else "singular",
                    what = text
                )
            )
            ImageUtils.drawCenteredString(graphics, floor, Rectangle(2, 2, 693, 100), font)

            val result = canvas.toByteArray(ImageFormatType.PNG)
            context.reply(false) {
                files += AttachedFile.fromData(result, "lavareverso.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val text = args.drop(1).joinToString(" ")
            if (text.isBlank()) {
                context.explain()
                return null
            }

            return mapOf(
                options.imageReference to ImageReferenceOrAttachment(args.getOrNull(0), context.getImage(0)),
                options.text to text
            )
        }
    }

    class LaranjoExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val text = string("text", I18nKeysData.Commands.Command.Laranjo.Options.Text.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val text = args[options.text]
            val template = (context.loritta.assets.loadImage("laranjo.png", loadFromCache = false) as JVMImage).handle as BufferedImage

            val graphics = template.createGraphics().apply { enableFontAntiAliasing() }
            graphics.color = Color.BLACK
            graphics.font = Font.createFont(Font.TRUETYPE_FONT, File(LorittaBot.ASSETS, "mavenpro-bold.ttf")).deriveFont(24F)

            ImageUtils.drawTextWrapSpaces(context.loritta, text, 2, 40, 334, 9999, graphics.fontMetrics, graphics)

            val result = template.toByteArray(ImageFormatType.PNG)
            context.reply(false) {
                files += AttachedFile.fromData(result, "laranjo.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val text = args.joinToString(" ")
            if (text.isBlank()) {
                context.explain()
                return null
            }

            return mapOf(options.text to text)
        }
    }

    class GumballExecutor : UnleashedLocalSingleImageGifCommandBase(
        "gumball.gif",
        { context, contextImage ->
            GumballGIF.getGIF(
                contextImage,
                context.i18nContext.get(I18nKeysData.Commands.Command.Gumball.Subtitle1),
                context.i18nContext.get(I18nKeysData.Commands.Command.Gumball.Subtitle2)
            )
        }
    )

    class DemonExecutor : UnleashedLocalSingleImageGifCommandBase(
        "demon.gif",
        { context, contextImage ->
            DemonGIF.getGIF(contextImage, context.config.localeId)
        }
    )

    class TriggeredExecutor : UnleashedLocalSingleImageGifCommandBase(
        "triggered.gif",
        { _, contextImage ->
            val triggeredLabel = readImage(File(LorittaBot.ASSETS, "triggered.png"))

            val subtractW = contextImage.width / 16
            val subtractH = contextImage.height / 16
            val inputWidth = contextImage.width - subtractW
            val inputHeight = contextImage.height - subtractH

            val labelHeight = triggeredLabel.height * inputWidth / triggeredLabel.width
            val scaledTriggeredLabel = triggeredLabel.getScaledInstance(inputWidth, labelHeight, BufferedImage.SCALE_SMOOTH)

            val base = BufferedImage(inputWidth, inputHeight + scaledTriggeredLabel.getHeight(null), BufferedImage.TYPE_INT_ARGB)
            val tint = BufferedImage(base.width, inputHeight, BufferedImage.TYPE_INT_ARGB)

            val tintGraphics = tint.graphics
            tintGraphics.color = Color(255, 0, 0, 60)
            tintGraphics.fillRect(0, 0, tint.width, tint.height)

            val outputFile = File(LorittaBot.TEMP + "triggered-" + System.currentTimeMillis() + ".gif")
            val output = FileImageOutputStream(outputFile)
            val writer = GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, 4, true)

            val graphics = base.graphics
            for (i in 0..5) {
                val offsetX = LorittaBot.RANDOM.nextInt(0, subtractW)
                val offsetY = LorittaBot.RANDOM.nextInt(0, subtractH)

                val subimage = contextImage.getSubimage(offsetX, offsetY, inputWidth, inputHeight)

                graphics.drawImage(subimage, 0, 0, null)
                graphics.drawImage(tint, 0, 0, null)
                graphics.drawImage(scaledTriggeredLabel, 0, inputHeight, null)
                writer.writeToSequence(base)
            }

            writer.close()
            output.close()

            outputFile
        }
    )

    class SwingExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val victim = imageReferenceOrAttachment("victim", I18nKeysData.Commands.Command.Swing.Options.Victim.Text)
            val attacker = imageReferenceOrAttachment("attacker", I18nKeysData.Commands.Command.Swing.Options.Attacker.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val victimUrl = args[options.victim].get(context)
            val attackerOption = args[options.attacker]
            // Attacker defaults to the user invoking the command if neither field was provided
            val attackerUrl = if (attackerOption.attachment == null && attackerOption.dataValue == null)
                context.user.effectiveAvatarUrl
            else
                attackerOption.get(context)

            val victimImage = LorittaUtils.downloadImage(context.loritta, victimUrl) ?: context.fail(false) {
                styled(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
            }
            val attackerImage = LorittaUtils.downloadImage(context.loritta, attackerUrl) ?: context.fail(false) {
                styled(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
            }

            val gifFile = SwingGIF.getGIF(victimImage, attackerImage)
            try {
                context.loritta.gifsicle.optimizeGIF(gifFile)
                val bytes = gifFile.readBytes()
                context.reply(false) {
                    files += AttachedFile.fromData(bytes, "swing.gif")
                }
            } finally {
                gifFile.delete()
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return mapOf(
                options.victim to ImageReferenceOrAttachment(args.getOrNull(0), context.getImage(0)),
                options.attacker to ImageReferenceOrAttachment(args.getOrNull(1), context.getImage(1))
            )
        }
    }

    class ContentAwareScaleExecutor : UnleashedLocalSingleImageCommandBase(
        "content_aware_scale.png",
        { _, contextImage ->
            val loriImage = LorittaImage(contextImage)
            loriImage.resize(256, 256, true)
            var newImage = loriImage.bufferedImage

            for (i in 0 until 256) {
                if (32 > newImage.height || 32 > newImage.width) break
                val direction = if (i % 2 == 0) SeamCarver.CarveDirection.HORIZONTAL else SeamCarver.CarveDirection.VERTICAL
                newImage = SeamCarver.carveSeam(newImage, direction)
            }

            newImage.toByteArray(ImageFormatType.PNG)
        }
    )
}
