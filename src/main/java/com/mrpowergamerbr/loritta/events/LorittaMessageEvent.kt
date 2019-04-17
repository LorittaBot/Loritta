package com.mrpowergamerbr.loritta.events

import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
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
        val serverConfig: MongoServerConfig,
        val locale: LegacyBaseLocale,
        val lorittaUser: LorittaUser
) {
	val jda: JDA get() = author.jda

	fun isFromType(type: ChannelType): Boolean {
		return this.channel.type == type
	}
}