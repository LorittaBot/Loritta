package net.perfectdreams.loritta.commands.images

import com.mrpowergamerbr.loritta.utils.toBufferedImage
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.ImageToAsciiConverter

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