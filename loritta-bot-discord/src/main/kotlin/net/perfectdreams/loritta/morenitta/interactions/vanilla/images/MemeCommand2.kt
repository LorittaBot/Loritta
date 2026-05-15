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
import java.awt.image.BufferedImage
import java.io.File
import java.util.*

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
    }

    class DiePlagueExecutor : UnleashedLocalSingleImageCommandBase(
        "die_plague.png",
        { context, contextImage ->
            val template = (context.loritta.assets.loadImage("die_plague.png", loadFromCache = false) as JVMImage).handle as BufferedImage
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
}
