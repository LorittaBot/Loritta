package net.perfectdreams.loritta.morenitta.interactions.commands

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateData

fun InlineMessage<MessageCreateData>.addFileData(
    name: String,
    data: ByteArray
) {
    files += FileUpload.fromData(data.inputStream(), name)
}