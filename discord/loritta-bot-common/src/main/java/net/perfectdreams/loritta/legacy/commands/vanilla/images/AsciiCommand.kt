package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.utils.toBufferedImage
import net.perfectdreams.loritta.legacy.api.commands.ArgumentType
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.api.commands.arguments
import net.perfectdreams.loritta.legacy.api.utils.image.JVMImage
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.platform.discord.legacy.commands.DiscordAbstractCommandBase

class AsciiCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("ascii", "asciiart", "img2ascii", "img2asciiart", "image2ascii"), CommandCategory.IMAGES) {
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
                    options.add(ImageToAsciiConverter.AsciiOptions.valueOf(arg.toUpperCase()))
                } catch (e: IllegalArgumentException) {
                }
            }
            val converter = ImageToAsciiConverter(*options.toTypedArray())
            val newImage = converter.imgToAsciiImg(img.handle.toBufferedImage())

            context.sendImage(JVMImage(newImage), "asciiart.png", context.getUserMention(true))
        }
    }
}