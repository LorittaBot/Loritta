package com.mrpowergamerbr.loritta.events

import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.*

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