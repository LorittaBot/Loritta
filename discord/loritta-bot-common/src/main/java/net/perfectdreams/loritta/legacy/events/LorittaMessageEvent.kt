package net.perfectdreams.loritta.legacy.events

import net.perfectdreams.loritta.legacy.dao.ServerConfig
import net.perfectdreams.loritta.legacy.utils.LorittaUser
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*

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