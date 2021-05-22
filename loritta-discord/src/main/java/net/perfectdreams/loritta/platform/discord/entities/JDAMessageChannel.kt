package net.perfectdreams.loritta.platform.discord.entities

import com.mrpowergamerbr.loritta.utils.extensions.await
import net.dv8tion.jda.api.MessageBuilder
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.MessageChannel

class JDAMessageChannel(private val channel: net.dv8tion.jda.api.entities.MessageChannel) : MessageChannel {
    override suspend fun sendMessage(message: LorittaMessage) {
        val builder = MessageBuilder(message.content)

        for (reply in message.replies) {
            builder.append('\n')
            builder.append("${reply.prefix} **|** ${reply.content}")
        }

        channel.sendMessage(builder.build()).await()
    }
}