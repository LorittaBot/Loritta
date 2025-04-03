package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.utils.toBufferedImage
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase

class AsciiCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("ascii", "asciiart", "img2ascii", "img2asciiart", "image2ascii"), net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command"
    }

    override fun command() = create {
        needsToUploadFiles = true

        localizedDescription("$LOCALE_PREFIX.ascii.description")
        localizedExamples("$LOCALE_PREFIX.ascii.examples")

        usage {
            arguments {
                argument(ArgumentType.IMAGE) {}
            }
            arguments {
                argument(ArgumentType.TEXT) {
                    optional = true
                }
            }
        }

        executesDiscord {
            val context = this

            val img = context.imageOrFail(0) as JVMImage
            val options = mutableSetOf<ImageToAsciiConverter.AsciiOptions>()
            for (arg in args) {
                try {
                    options.add(ImageToAsciiConverter.AsciiOptions.valueOf(arg.uppercase()))
                } catch (e: IllegalArgumentException) {
                }
            }
            val converter = ImageToAsciiConverter(loritta, *options.toTypedArray())
            val newImage = converter.imgToAsciiImg(img.handle.toBufferedImage())

            context.sendImage(JVMImage(newImage), "asciiart.png", context.getUserMention(true))
        }
    }
}