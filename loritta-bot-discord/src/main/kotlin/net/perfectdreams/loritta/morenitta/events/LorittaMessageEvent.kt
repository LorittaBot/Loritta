package net.perfectdreams.loritta.morenitta.events

import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

class LorittaMessageEvent(
    val author: User,
    val member: Member?,
    val message: Message,
    val messageId: String,
    val guild: Guild?,
    val channel: MessageChannel,
    val textChannel: TextChannel?,
    val serverConfig: ServerConfig,
    val locale: BaseLocale,
    val lorittaUser: LorittaUser
) {
	val jda: JDA get() = author.jda

	fun isFromType(type: ChannelType): Boolean {
		return this.channel.type == type
	}
}