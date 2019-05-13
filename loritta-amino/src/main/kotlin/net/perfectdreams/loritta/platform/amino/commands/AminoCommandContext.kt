package net.perfectdreams.loritta.platform.amino.commands

import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.aminoreapi.events.message.MessageReceivedEvent
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import net.perfectdreams.loritta.api.entities.Guild
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.MessageChannel
import net.perfectdreams.loritta.api.platform.LorittaBot
import net.perfectdreams.loritta.platform.amino.entities.AminoMessage
import net.perfectdreams.loritta.platform.amino.entities.AminoMessageChannel
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO

class AminoCommandContext(val loritta: LorittaBot, val event: MessageReceivedEvent, locale: BaseLocale, legacyLocale: LegacyBaseLocale, command: LorittaCommand, args: Array<String>) : LorittaCommandContext(locale, legacyLocale, command, args) {
    override val message: Message
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val guild: Guild?
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val channel: MessageChannel
        get() = AminoMessageChannel()

    override fun getAsMention(addSpace: Boolean): String {
        var output = event.message.author.nickname
        if (addSpace)
            output += " "
        return output
    }

    override suspend fun reply(message: String, prefix: String?, forceMention: Boolean): Message {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun reply(vararg loriReplies: LoriReply): Message {
        return reply(false, *loriReplies)
    }

    override suspend fun reply(mentionUserBeforeReplies: Boolean, vararg loriReplies: LoriReply): Message {
        val message = StringBuilder()
        if (mentionUserBeforeReplies) {
            message.append(LoriReply().build(this))
            message.append("\n")
        }
        for (loriReply in loriReplies) {
            message.append(loriReply.build(this))
            message.append("\n")
        }
        return sendMessage(message.toString())
    }

    override suspend fun reply(image: BufferedImage, fileName: String, vararg loriReplies: LoriReply): Message {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun sendMessage(message: String): Message {
        val thread = event.retrieveThread() ?: throw UnsupportedOperationException()
        thread.sendMessage(message)
        return AminoMessage(message)
    }

    override suspend fun sendFile(file: File, name: String, message: String): Message {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun sendFile(inputStream: InputStream, name: String, message: String): Message {
        val thread = event.retrieveThread() ?: throw UnsupportedOperationException()
        thread.sendImage(ImageIO.read(inputStream))
        return AminoMessage(message)
    }

    override suspend fun getImage(text: String, search: Int, avatarSize: Int): BufferedImage? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getUserAt(argument: Int): User? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getUser(link: String?): net.perfectdreams.loritta.api.entities.User? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getImageUrlAt(argument: Int, search: Int, avatarSize: Int): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getImageUrl(link: String?, search: Int, avatarSize: Int): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getImageAt(argument: Int, search: Int, avatarSize: Int): BufferedImage? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun explain() {
        val thread = event.retrieveThread() ?: return
        thread.sendMessage("Help for ${command::class.simpleName}")
        thread.sendMessage(command.getUsage(locale).build(locale))
    }
}