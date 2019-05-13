package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ChannelListener(val loritta: Loritta) : ListenerAdapter() {
	override fun onTextChannelCreate(event: TextChannelCreateEvent) {
		loritta.executor.execute {
			val config = loritta.getServerConfigForGuild(event.guild.id)

			if (config.miscellaneousConfig.enableQuirky)
				event.channel.sendMessage("First! <:lori_owo:417813932380520448>").queue()
		}
	}
}