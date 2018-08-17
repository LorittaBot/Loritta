package com.mrpowergamerbr.loritta.events

import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.*

class LorittaMessageEvent(
		val author: User,
		val member: Member?,
		val message: Message,
		val messageId: String,
		val guild: Guild?,
		val channel: MessageChannel,
		val textChannel: TextChannel?
) {
	val jda: JDA get() = author.jda

	fun isFromType(type: ChannelType): Boolean {
		return this.channel.type == type
	}
}