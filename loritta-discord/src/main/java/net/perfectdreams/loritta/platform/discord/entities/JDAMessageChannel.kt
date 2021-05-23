package net.perfectdreams.loritta.platform.discord.entities

import com.mrpowergamerbr.loritta.utils.extensions.await
import net.dv8tion.jda.api.MessageBuilder
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.MessageChannel

class JDAMessageChannel(internal val channel: net.dv8tion.jda.api.entities.MessageChannel) : MessageChannel {
    override suspend fun sendMessage(message: LorittaMessage) {
        val builder = MessageBuilder(message.content)

        for (reply in message.replies) {
            builder.append('\n')
            builder.append("${reply.prefix} **|** ${reply.content}")
        }

        builder.mentionUsers(*message.allowedMentions.users.map { it.id }.toLongArray())

        channel.sendMessage(builder.build())
            .apply {
                message.files.forEach {
                    this.addFile(it.value, it.key)
                }
            }
            .await()
    }
}