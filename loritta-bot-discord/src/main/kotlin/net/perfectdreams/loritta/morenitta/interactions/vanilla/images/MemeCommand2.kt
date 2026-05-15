package net.perfectdreams.loritta.morenitta.interactions.vanilla.images

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageFormatType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.ImageUtils.toByteArray
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.utils.extensions.enableFontAntiAliasing
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.SlashCommandDeclarationWrapper
import net.perfectdreams.loritta.morenitta.interactions.commands.slashCommand
import net.perfectdreams.loritta.morenitta.interactions.vanilla.images.base.UnleashedLocalSingleImageCommandBase
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import java.awt.Color
import java.awt.Font
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
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
}
