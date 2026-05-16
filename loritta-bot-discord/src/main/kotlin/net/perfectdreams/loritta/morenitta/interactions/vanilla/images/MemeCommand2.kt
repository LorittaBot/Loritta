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
import net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base.UnleashedSingleImageOptions
import net.perfectdreams.loritta.morenitta.utils.ImageToAsciiConverter
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.makeRoundedCorners
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.EmojiMasher
import net.perfectdreams.loritta.morenitta.utils.SeamCarver
import net.perfectdreams.loritta.morenitta.utils.TretaNewsGenerator
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.perfectdreams.loritta.morenitta.utils.extensions.getEffectiveAvatarUrl
import net.perfectdreams.loritta.morenitta.utils.toBufferedImage
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.awt.image.FilteredImageSource
import java.awt.image.RGBImageFilter
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

        subcommandGroup(I18nKeysData.Commands.Command.Mirror.Label, I18nKeysData.Commands.Command.Mirror.Description) {
            subcommand(I18nKeysData.Commands.Command.Mirror.Jooj.Label, I18nKeysData.Commands.Command.Mirror.Jooj.Description, UUID.fromString("6d7c50e2-f901-422e-9f49-cc56bc4176b8")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("jooj")
                }

                executor = JoojExecutor()
            }

            subcommand(I18nKeysData.Commands.Command.Mirror.Ojjo.Label, I18nKeysData.Commands.Command.Mirror.Ojjo.Description, UUID.fromString("62d6f6a2-edbc-4098-a908-61dd4e186fe7")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("ojjo")
                }

                executor = OjjoExecutor()
            }
        }

        subcommandGroup(I18nKeysData.Commands.Command.Lava.Label, I18nKeysData.Commands.Command.Lava.Description) {
            subcommand(I18nKeysData.Commands.Command.Lava.Normal.Label, I18nKeysData.Commands.Command.Lava.Normal.Description, UUID.fromString("9e9de728-64b4-4ea5-8c9b-461a76213eaf")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("lava")
                }

                executor = LavaExecutor()
            }

            subcommand(I18nKeysData.Commands.Command.Lava.Reverso.Label, I18nKeysData.Commands.Command.Lava.Reverso.Description, UUID.fromString("edbe6d45-db98-430e-8a17-6c9f48f7f977")) {
                alternativeLegacyAbsoluteCommandPaths.apply {
                    add("lavareverse")
                    add("lavareverso")
                    add("reverselava")
                }

                executor = LavaReverseExecutor()
            }
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

        subcommand(I18nKeysData.Commands.Command.Tretanews.Label, I18nKeysData.Commands.Command.Tretanews.Description, UUID.fromString("7ab06852-8886-4b9c-95a4-1a4349e48c81")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("tretanews")
            }

            executor = TretaNewsExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Firstwords.Label, I18nKeysData.Commands.Command.Firstwords.Description, UUID.fromString("312c93b7-f5c3-45b1-a207-92dd58fc8042")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("firstwords")
                add("primeiraspalavras")
            }

            executor = FirstWordsExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Riplife.Label, I18nKeysData.Commands.Command.Riplife.Description, UUID.fromString("1bad9b07-b481-4d28-9fdb-16712de39a80")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("riplife")
                add("ripvida")
            }

            executor = RipLifeExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Perfect.Label, I18nKeysData.Commands.Command.Perfect.Description, UUID.fromString("3dd0b19b-7703-4f09-8d65-abfac518809c")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("perfect")
                add("perfeito")
            }

            executor = PerfectExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Nyan.Label, I18nKeysData.Commands.Command.Nyan.Description, UUID.fromString("93dea525-1cd9-4eea-82d7-171b5f7aee37")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("nyan")
            }

            executor = NyanExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Gods.Label, I18nKeysData.Commands.Command.Gods.Description, UUID.fromString("44adebd1-e4e1-4c4f-8851-1fcff78dd97a")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("deuses")
            }

            executor = GodsExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Friendship.Label, I18nKeysData.Commands.Command.Friendship.Description, UUID.fromString("a4c20758-0cce-40e1-ba29-1f94dc08160f")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("friendship")
                add("amizade")
            }

            executor = FriendshipExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Reasons.Label, I18nKeysData.Commands.Command.Reasons.Description, UUID.fromString("4031d888-e607-41bf-b657-6e8d7e73d445")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("reasons")
                add("razões")
                add("razoes")
            }

            executor = ReasonsExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Emojimashup.Label, I18nKeysData.Commands.Command.Emojimashup.Description, UUID.fromString("5b3e6729-c492-4886-bc81-2c1eb4072265")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("emojimashup")
                add("emojismashup")
                add("mashupemoji")
                add("mashupemojis")
                add("misturaremojis")
                add("misturaremoji")
            }

            executor = EmojiMashupExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Gang.Label, I18nKeysData.Commands.Command.Gang.Description, UUID.fromString("b352a867-7e11-4d73-899f-435d260c0a0c")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("gang")
                add("gangue")
            }

            executor = GangueExecutor()
        }

        subcommand(I18nKeysData.Commands.Command.Ascii.Label, I18nKeysData.Commands.Command.Ascii.Description, UUID.fromString("0920936d-c9cd-4cff-b82d-d404ccc40a20")) {
            alternativeLegacyAbsoluteCommandPaths.apply {
                add("ascii")
                add("asciiart")
                add("img2ascii")
                add("img2asciiart")
                add("image2ascii")
            }

            executor = AsciiExecutor()
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
            val text = string("text", I18nKeysData.Commands.Command.Lava.Normal.Options.Text.Text)
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
                options.imageReference to context.getImageReferenceOrAttachment(0),
                options.text to text
            )
        }
    }

    class LavaReverseExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val text = string("text", I18nKeysData.Commands.Command.Lava.Reverso.Options.Text.Text)
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
                options.imageReference to context.getImageReferenceOrAttachment(0),
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
                options.victim to context.getImageReferenceOrAttachment(0),
                options.attacker to context.getImageReferenceOrAttachment(1)
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

    class TretaNewsExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val user1 = user("user1", I18nKeysData.Commands.Command.Tretanews.Options.User1.Text)
            val user2 = user("user2", I18nKeysData.Commands.Command.Tretanews.Options.User2.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val user1 = args[options.user1].user
            val user2 = args[options.user2].user

            val base = TretaNewsGenerator.generate(context.loritta, context.guildOrNull, user1, user2)
            val imageBytes = base.image.toByteArray(ImageFormatType.PNG)

            context.reply(false) {
                styled(
                    context.i18nContext.get(I18nKeysData.Commands.Command.Tretanews.Intro),
                    "<:fluffy:372454445721845761>"
                )
                styled("`${base.title}`")
                styled(
                    context.i18nContext.get(
                        I18nKeysData.Commands.Command.Tretanews.Stats(
                            views = base.views,
                            viewsLabel = context.i18nContext.get(I18nKeysData.Commands.Command.Tretanews.Views),
                            likes = base.likes,
                            likesLabel = context.i18nContext.get(I18nKeysData.Commands.Command.Tretanews.Likes),
                            dislikes = base.dislikes,
                            dislikesLabel = context.i18nContext.get(I18nKeysData.Commands.Command.Tretanews.Dislikes),
                        )
                    ),
                    "📈"
                )
                files += AttachedFile.fromData(imageBytes, "tretanews.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val user1 = context.getUserAndMember(0)
            val user2 = context.getUserAndMember(1)
            if (user1 == null || user2 == null) {
                context.explain()
                return null
            }
            return mapOf(
                options.user1 to user1,
                options.user2 to user2
            )
        }
    }

    class FirstWordsExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val text = string("text", I18nKeysData.Commands.Command.Firstwords.Options.Text.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val text = args[options.text]
            val template = (context.loritta.assets.loadImage("tirinha_baby.png", loadFromCache = false) as JVMImage).handle as BufferedImage

            val graphics = template.createGraphics().apply { enableFontAntiAliasing() }
            graphics.color = Color(0, 0, 0, 255)
            graphics.font = Font("Arial", Font.BOLD, 32)

            val firstChar = text[0]
            val babbling = "$firstChar... $firstChar..."

            ImageUtils.drawTextWrap(context.loritta, babbling, 4, 5 + graphics.font.size, 236, 0, graphics.fontMetrics, graphics)
            ImageUtils.drawTextWrapSpaces(context.loritta, text, 4, 277 + graphics.font.size, 342, 0, graphics.fontMetrics, graphics)

            val result = template.toByteArray(ImageFormatType.PNG)
            context.reply(false) {
                files += AttachedFile.fromData(result, "tirinha_baby.png")
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

    class RipLifeExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override val options = UnleashedSingleImageOptions()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val imageUrl = args[options.imageReference].get(context)
            val contextImage = LorittaUtils.downloadImage(context.loritta, imageUrl) ?: context.fail(false) {
                styled(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
            }

            val fileName = context.i18nContext.get(I18nKeysData.Commands.Command.Riplife.File)
            val template = (context.loritta.assets.loadImage(fileName, loadFromCache = false) as JVMImage).handle as BufferedImage

            val scaled = contextImage.getScaledInstance(133, 133, BufferedImage.SCALE_SMOOTH)
            template.graphics.drawImage(scaled, 133, 0, null)

            val result = template.toByteArray(ImageFormatType.PNG)
            context.reply(false) {
                files += AttachedFile.fromData(result, fileName)
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return mapOf(
                options.imageReference to context.getImageReferenceOrAttachment(0)
            )
        }
    }

    class NyanExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val text = optionalString("text", I18nKeysData.Commands.Command.Nyan.Options.Text.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val text = args[options.text]
            val isDog = text != null && text.replace(Regex("(.)\\1+"), "$1").equals("dog", ignoreCase = true)
            val times = if (text != null) text.count { it == (if (isDog) 'o' else 'a') } else 0

            val prefix = if (isDog) "dog" else "cat"
            val suffix = if (isDog) "" else "_v2"
            val left = (context.loritta.assets.loadImage("${prefix}_left$suffix.png", loadFromCache = false) as JVMImage).handle as BufferedImage
            val right = (context.loritta.assets.loadImage("${prefix}_right$suffix.png", loadFromCache = false) as JVMImage).handle as BufferedImage
            val middle = (context.loritta.assets.loadImage("${prefix}_middle$suffix.png", loadFromCache = false) as JVMImage).handle as BufferedImage

            val canvas = BufferedImage(
                left.width + right.width + middle.width * times,
                left.height,
                BufferedImage.TYPE_INT_ARGB
            )
            val canvasGraphics = canvas.graphics

            var x = 0
            canvasGraphics.drawImage(left, x, 0, null)
            x += left.width

            // Usar a cor rosa-meio-roxo que o Nyan Cat tem
            val dotColor = if (isDog) Color(243, 254, 255) else Color(255, 51, 153)
            repeat(times) {
                // Nós iremos "clonar" o nosso cat middle para colocar alguns pontinhos rosas aleatórios :)
                val middleCopy = BufferedImage(middle.width, middle.height, BufferedImage.TYPE_INT_ARGB)
                val mcGraphics = middleCopy.graphics
                mcGraphics.drawImage(middle, 0, 0, null)
                mcGraphics.color = dotColor

                val randomDots = LorittaBot.RANDOM.nextInt(0, 6)
                val invalidX = mutableListOf<Int>()
                val invalidY = mutableListOf<Int>()
                for (i in 0..randomDots) {
                    val randomX = LorittaBot.RANDOM.nextInt(0, 2)
                    val randomY = LorittaBot.RANDOM.nextInt(2, 16)
                    if (randomX in invalidX || (randomX - 1) in invalidX || (randomX + 1) in invalidX) continue
                    if (randomY in invalidY || (randomY - 1) in invalidY || (randomY + 1) in invalidY) continue
                    // Sabia que width/height 0 = 1px? Agora você sabe!
                    // Faça um retângulo 1x1 na nossa coordenada aleatória
                    mcGraphics.drawRect(randomX, randomY, 0, 0)
                    invalidX += randomX
                    invalidY += randomY
                }

                // E depois desenhe nossa imagem modificada na imagem original
                canvasGraphics.drawImage(middleCopy, x, 0, null)
                x += middleCopy.width
            }

            canvasGraphics.drawImage(right, x, 0, null)

            if (isDog) {
                // Desenhar as orelhas do dog
                val dogEars = (context.loritta.assets.loadImage("dog_ears.png", loadFromCache = false) as JVMImage).handle as BufferedImage
                canvasGraphics.drawImage(dogEars, canvas.width - 21, 5, null)
            }

            val scaled = canvas.getScaledInstance(canvas.width * 4, canvas.height * 4, BufferedImage.SCALE_AREA_AVERAGING).toBufferedImage()
            val result = scaled.toByteArray(ImageFormatType.PNG)
            context.reply(false) {
                files += AttachedFile.fromData(result, "nyan_cat.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return mapOf(options.text to args.singleOrNull())
        }
    }

    class PerfectExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override val options = UnleashedSingleImageOptions()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val imageUrl = args[options.imageReference].get(context)
            val contextImage = LorittaUtils.downloadImage(context.loritta, imageUrl) ?: context.fail(false) {
                styled(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
            }

            val template = (context.loritta.assets.loadImage("perfeito.png", loadFromCache = false) as JVMImage).handle as BufferedImage
            val scaled = contextImage.getScaledInstance(231, 231, BufferedImage.SCALE_SMOOTH)
            template.graphics.drawImage(scaled, 225, 85, null)

            val result = template.toByteArray(ImageFormatType.PNG)
            context.reply(false) {
                files += AttachedFile.fromData(result, "perfeito.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return mapOf(
                options.imageReference to context.getImageReferenceOrAttachment(0)
            )
        }
    }

    class GodsExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val text = string("text", I18nKeysData.Commands.Command.Gods.Options.Text.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val text = args[options.text]
            val template = (context.loritta.assets.loadImage("deuses.png", loadFromCache = false) as JVMImage).handle as BufferedImage

            // Vamos criar o nosso tempalte
            val image = BufferedImage(630, 830, BufferedImage.TYPE_INT_ARGB)
            val graphics = image.createGraphics().apply { enableFontAntiAliasing() }
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, 630, 830)
            graphics.color = Color.BLACK
            graphics.drawImage(template, 0, 200, null)

            val font = Font.createFont(Font.TRUETYPE_FONT, File(LorittaBot.ASSETS, "mavenpro-bold.ttf")).deriveFont(42F)
            graphics.font = font
            ImageUtils.drawTextWrapSpaces(context.loritta, text, 2, 40, 630, 9999, graphics.fontMetrics, graphics)

            val result = image.toByteArray(ImageFormatType.PNG)
            context.reply(false) {
                files += AttachedFile.fromData(result, "deuses.png")
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

    class FriendshipExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val oldFriend = user("oldfriend", I18nKeysData.Commands.Command.Friendship.Options.Oldfriend.Text)
            val newFriend = user("newfriend", I18nKeysData.Commands.Command.Friendship.Options.Newfriend.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val oldFriend = args[options.oldFriend].user
            val newFriend = args[options.newFriend].user

            val avatar = LorittaUtils.downloadImage(context.loritta, context.user.getEffectiveAvatarUrl(ImageFormat.PNG, 128)) ?: context.fail(false) {
                styled(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
            }
            val avatar2 = LorittaUtils.downloadImage(context.loritta, oldFriend.getEffectiveAvatarUrl(ImageFormat.PNG, 128)) ?: context.fail(false) {
                styled(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
            }
            val avatar3 = LorittaUtils.downloadImage(context.loritta, newFriend.getEffectiveAvatarUrl(ImageFormat.PNG, 128)) ?: context.fail(false) {
                styled(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
            }

            val template = (context.loritta.assets.loadImage("amizade.png", loadFromCache = false) as JVMImage).handle as BufferedImage

            val graphics = template.createGraphics().apply { enableFontAntiAliasing() } // É necessário usar Graphics2D para usar gradients

            // Colocar todos os avatares
            graphics.drawImage(avatar.getScaledInstance(108, 108, BufferedImage.SCALE_SMOOTH), 55, 10, null)
            graphics.drawImage(avatar3.getScaledInstance(110, 110, BufferedImage.SCALE_SMOOTH), 232, 54, null)
            graphics.drawImage(avatar2.getScaledInstance(85, 134, BufferedImage.SCALE_SMOOTH), 0, 166, null)
            graphics.drawImage(avatar2.getScaledInstance(111, 120, BufferedImage.SCALE_SMOOTH), 289, 180, null)

            // E colocar o overlay da imagem
            val overlay = (context.loritta.assets.loadImage("amizade_overlay.png", loadFromCache = false) as JVMImage).handle as BufferedImage
            graphics.drawImage(overlay, 0, 0, null)

            var font = graphics.font.deriveFont(21F)
            graphics.font = font
            var fontMetrics = graphics.getFontMetrics(font)

            val friendshipEnded = context.i18nContext.get(
                I18nKeysData.Commands.Command.Friendship.FriendWith(userName = oldFriend.name)
            )
            var gp = GradientPaint(
                0.0f, 0.0f,
                Color(202, 72, 15),
                0.0f, fontMetrics.height.toFloat() + fontMetrics.height.toFloat(),
                Color(66, 181, 33)
            )
            graphics.paint = gp

            ImageUtils.drawCenteredStringOutlined(graphics, friendshipEnded, Rectangle(0, 10, 400, 30), font)
            graphics.color = Color.RED

            font = font.deriveFont(30F)
            graphics.font = font

            ImageUtils.drawCenteredStringOutlined(
                graphics,
                context.i18nContext.get(I18nKeysData.Commands.Command.Friendship.Ended),
                Rectangle(0, 30, 400, 40),
                font
            )

            font = font.deriveFont(24F)
            graphics.font = font
            fontMetrics = graphics.getFontMetrics(font)
            gp = GradientPaint(
                0.0f, 140f,
                Color(206, 7, 129),
                0.0f, 190f,
                Color(103, 216, 11)
            )
            graphics.paint = gp
            // graphics.fillRect(0, 0, 400, 300); // debugging
            ImageUtils.drawCenteredStringOutlined(
                graphics,
                context.i18nContext.get(I18nKeysData.Commands.Command.Friendship.NowUser(userName = newFriend.name)),
                Rectangle(0, 100, 400, 110),
                font
            )
            ImageUtils.drawCenteredStringOutlined(
                graphics,
                context.i18nContext.get(I18nKeysData.Commands.Command.Friendship.IsMy),
                Rectangle(0, 120, 400, 130),
                font
            )
            graphics.color = Color.MAGENTA
            ImageUtils.drawCenteredStringOutlined(
                graphics,
                context.i18nContext.get(I18nKeysData.Commands.Command.Friendship.BestFriend),
                Rectangle(0, 140, 400, 150),
                font
            )

            val result = template.toByteArray(ImageFormatType.PNG)
            context.reply(false) {
                files += AttachedFile.fromData(result, "rip_amizade.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val oldFriend = context.getUserAndMember(0)
            val newFriend = context.getUserAndMember(1)
            if (oldFriend == null || newFriend == null) {
                context.explain()
                return null
            }
            return mapOf(
                options.oldFriend to oldFriend,
                options.newFriend to newFriend
            )
        }
    }

    class ReasonsExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        override val options = UnleashedSingleImageOptions()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val imageUrl = args[options.imageReference].get(context)
            val contextImage = LorittaUtils.downloadImage(context.loritta, imageUrl) ?: context.fail(false) {
                styled(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
            }

            var template = (context.loritta.assets.loadImage("reasons.png", loadFromCache = false) as JVMImage).handle as BufferedImage
            val image = BufferedImage(346, 600, BufferedImage.TYPE_INT_ARGB)

            val graphics = image.graphics
            val skewed = LorittaImage(contextImage)

            skewed.resize(202, 202)

            // Vamos baixar o avatar do usuário
            val avatar = LorittaUtils.downloadImage(context.loritta, context.user.getEffectiveAvatarUrl(ImageFormat.PNG, 128)) ?: context.fail(false) {
                styled(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
            }

            // Agora nós iremos pegar a cor mais prevalente na imagem do avatar do usuário
            val dominantImage = avatar.getScaledInstance(1, 1, BufferedImage.SCALE_AREA_AVERAGING).toBufferedImage()
            val dominantColor = dominantImage.getRGB(0, 0)

            val red = (dominantColor shr 16) and 0xFF
            val green = (dominantColor shr 8) and 0xFF
            val blue = dominantColor and 0xFF

            // Aplicar nosso filtro
            val colorFilter = MagentaDominantSwapFilter(red, green, blue)

            val newTemplate = FilteredImageSource(template.source, colorFilter)
            template = Toolkit.getDefaultToolkit().createImage(newTemplate).toBufferedImage()

            skewed.width = 240 // Aumentar o tamanho da imagem para manipular ela
            skewed.height = 240
            // skew image
            skewed.setCorners(
                // keep the upper left corner as it is
                0F, 0F, // UL

                // push the upper right corner more to the bottom
                202 - 40F, 40F, // UR

                // push the lower right corner more to the left
                236F, 210F, // LR

                // push the lower left corner more to the right
                95F, 215F // LL
            )

            graphics.drawImage(skewed.bufferedImage, 30, 370, null)

            graphics.drawImage(template, 0, 0, null) // Desenhe o template por cima!

            // Agora nós vamos colar o avatar em cima do template
            // Vamos usar o javaxt porque é bem mais fácil
            val rotatedAvatar = LorittaImage(avatar)
            rotatedAvatar.resize(109, 109)
            rotatedAvatar.rotate(5.0)
            graphics.drawImage(rotatedAvatar.bufferedImage, 188, 4, null)

            val result = image.toByteArray(ImageFormatType.PNG)
            context.reply(false) {
                files += AttachedFile.fromData(result, "reasons.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return mapOf(
                options.imageReference to context.getImageReferenceOrAttachment(0)
            )
        }

        private class MagentaDominantSwapFilter(
            val newR: Int,
            val newG: Int,
            val newB: Int
        ) : RGBImageFilter() {
            init {
                canFilterIndexColorModel = false
            }

            override fun filterRGB(x: Int, y: Int, rgb: Int): Int {
                val red = (rgb shr 16) and 0xFF
                val green = (rgb shr 8) and 0xFF
                val blue = rgb and 0xFF

                if (red == 255 && green == 0 && blue == 255) {
                    return Color(newR, newB, newG).rgb
                }
                return rgb
            }
        }
    }

    class EmojiMashupExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val emoji1 = string("emoji1", I18nKeysData.Commands.Command.Emojimashup.Options.Emoji1.Text)
            val emoji2 = string("emoji2", I18nKeysData.Commands.Command.Emojimashup.Options.Emoji2.Text)
            val emoji3 = optionalString("emoji3", I18nKeysData.Commands.Command.Emojimashup.Options.Emoji3.Text)
            val emoji4 = optionalString("emoji4", I18nKeysData.Commands.Command.Emojimashup.Options.Emoji4.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val emojiMasher = EmojiMasher(File(context.loritta.config.loritta.folders.assets, "emoji_mashup"))

            suspend fun resolve(arg: String): String? {
                val unicode = try {
                    LorittaUtils.toUnicode(arg.codePointAt(0)).substring(2)
                } catch (e: Exception) {
                    return null
                }
                return if (emojiMasher.isEmojiSupported(unicode)) unicode else null
            }

            suspend fun failInvalid(arg: String): Nothing = context.fail(false) {
                if (arg.startsWith("<")) {
                    styled(
                        context.i18nContext.get(
                            I18nKeysData.Commands.Command.Emojimashup.InvalidEmojiDiscord(emoji = arg.stripCodeMarks())
                        ),
                        Emotes.LoriSob
                    )
                } else {
                    styled(
                        context.i18nContext.get(
                            I18nKeysData.Commands.Command.Emojimashup.InvalidEmoji(emoji = arg.stripCodeMarks())
                        ),
                        Emotes.LoriSob
                    )
                }
            }

            val emojiArg1 = args[options.emoji1]
            val emojiArg2 = args[options.emoji2]
            val emojiArg3 = args[options.emoji3]
            val emojiArg4 = args[options.emoji4]

            val emoji1Code = resolve(emojiArg1) ?: failInvalid(emojiArg1)
            val emoji2Code = resolve(emojiArg2) ?: failInvalid(emojiArg2)
            val emoji3Code = emojiArg3?.let { resolve(it) ?: failInvalid(it) }
            val emoji4Code = emojiArg4?.let { resolve(it) ?: failInvalid(it) }

            val image = emojiMasher.mashupEmojis(emoji1Code, emoji2Code, emoji3Code, emoji4Code)

            val result = image.toByteArray(ImageFormatType.PNG)
            context.reply(false) {
                files += AttachedFile.fromData(result, "emoji_mashup.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?>? {
            val emoji1 = args.getOrNull(0)
            val emoji2 = args.getOrNull(1)
            if (emoji1 == null || emoji2 == null) {
                context.explain()
                return null
            }
            return mapOf(
                options.emoji1 to emoji1,
                options.emoji2 to emoji2,
                options.emoji3 to args.getOrNull(2),
                options.emoji4 to args.getOrNull(3)
            )
        }
    }

    class GangueExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val friend1 = imageReferenceOrAttachment("friend1", TodoFixThisData)
            val friend2 = imageReferenceOrAttachment("friend2", TodoFixThisData)
            val friend3 = imageReferenceOrAttachment("friend3", TodoFixThisData)
            val friend4 = imageReferenceOrAttachment("friend4", TodoFixThisData)
            val friend5 = imageReferenceOrAttachment("friend5", TodoFixThisData)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            suspend fun download(reference: ImageReferenceOrAttachment): BufferedImage {
                val url = reference.get(context)
                return LorittaUtils.downloadImage(context.loritta, url) ?: context.fail(false) {
                    styled(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
                }
            }

            val image1 = download(args[options.friend1])
            val image2 = download(args[options.friend2])
            val image3 = download(args[options.friend3])
            val image4 = download(args[options.friend4])
            val image5 = download(args[options.friend5])

            val template = (context.loritta.assets.loadImage("cocielo/cocielo.png", loadFromCache = false) as JVMImage).handle as BufferedImage
            val overlay = (context.loritta.assets.loadImage("cocielo/overlay.png", loadFromCache = false) as JVMImage).handle as BufferedImage

            val scaled = image1.getScaledInstance(59, 59, BufferedImage.SCALE_SMOOTH)
                .toBufferedImage()
                .makeRoundedCorners(20)
            val scaled2 = image2.getScaledInstance(47, 57, BufferedImage.SCALE_SMOOTH)
                .toBufferedImage()
                .makeRoundedCorners(20)
            val scaled3 = image3.getScaledInstance(50, 50, BufferedImage.SCALE_SMOOTH)
                .toBufferedImage()
                .makeRoundedCorners(20)
            val scaled4 = image4.getScaledInstance(53, 58, BufferedImage.SCALE_SMOOTH)
                .toBufferedImage()
                .makeRoundedCorners(20)
            val scaled5 = image5.getScaledInstance(43, 43, BufferedImage.SCALE_SMOOTH)
                .toBufferedImage()
                .makeRoundedCorners(20)

            // Porque nós precisamos rotacionar
            val rotated = LorittaImage(scaled5)
            rotated.rotate(335.0)

            template.graphics.drawImage(scaled, 216, 80, null)
            template.graphics.drawImage(scaled2, 142, 87, null)
            template.graphics.drawImage(scaled3, 345, 80, null)
            template.graphics.drawImage(scaled4, 28, 141, null)
            template.graphics.drawImage(rotated.bufferedImage, 290, -5, null)
            template.graphics.drawImage(overlay, 0, 0, null)

            val result = template.toByteArray(ImageFormatType.PNG)
            context.reply(false) {
                files += AttachedFile.fromData(result, "gangue.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            return mapOf(
                options.friend1 to context.getImageReferenceOrAttachment(0),
                options.friend2 to context.getImageReferenceOrAttachment(1),
                options.friend3 to context.getImageReferenceOrAttachment(2),
                options.friend4 to context.getImageReferenceOrAttachment(3),
                options.friend5 to context.getImageReferenceOrAttachment(4)
            )
        }
    }

    class AsciiExecutor : LorittaSlashCommandExecutor(), LorittaLegacyMessageCommandExecutor {
        class Options : ApplicationCommandOptions() {
            val imageReference = imageReferenceOrAttachment("image", TodoFixThisData)
            val colorize = optionalBoolean("colorize", I18nKeysData.Commands.Command.Ascii.Options.Colorize.Text)
            val dither = optionalBoolean("dither", I18nKeysData.Commands.Command.Ascii.Options.Dither.Text)
        }

        override val options = Options()

        override suspend fun execute(context: UnleashedContext, args: SlashCommandArguments) {
            context.deferChannelMessage(false)

            val imageUrl = args[options.imageReference].get(context)
            val contextImage = LorittaUtils.downloadImage(context.loritta, imageUrl) ?: context.fail(false) {
                styled(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
            }

            val asciiOptions = buildList {
                if (args[options.colorize] == true) add(ImageToAsciiConverter.AsciiOptions.COLORIZE)
                if (args[options.dither] == true) add(ImageToAsciiConverter.AsciiOptions.DITHER)
            }

            val converter = ImageToAsciiConverter(context.loritta, *asciiOptions.toTypedArray())
            val newImage = converter.imgToAsciiImg(contextImage)

            val result = newImage.toByteArray(ImageFormatType.PNG)
            context.reply(false) {
                files += AttachedFile.fromData(result, "asciiart.png")
            }
        }

        override suspend fun convertToInteractionsArguments(
            context: LegacyMessageCommandContext,
            args: List<String>
        ): Map<OptionReference<*>, Any?> {
            val flags = args.drop(1).map { it.lowercase() }
            return mapOf(
                options.imageReference to context.getImageReferenceOrAttachment(0),
                options.colorize to ("colorize" in flags),
                options.dither to ("dither" in flags)
            )
        }
    }
}
