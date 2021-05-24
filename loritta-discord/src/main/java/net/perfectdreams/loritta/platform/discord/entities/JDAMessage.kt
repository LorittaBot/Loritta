package net.perfectdreams.loritta.platform.discord.entities

import net.perfectdreams.loritta.common.entities.Message

class JDAMessage(private val message: net.dv8tion.jda.api.entities.Message) : Message {
    override val id: Long
        get() = message.idLong
}