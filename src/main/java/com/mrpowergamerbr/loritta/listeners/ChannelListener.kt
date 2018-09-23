package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.ignoreRequest
import net.dv8tion.jda.core.events.channel.text.TextChannelCreateEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter

class ChannelListener(val loritta: Loritta) : ListenerAdapter() {
	override fun onTextChannelCreate(event: TextChannelCreateEvent) {
		if (event.channel.canTalk()) {
			if (ignoreRequest())
				return

			loritta.executor.execute {
				val config = loritta.getServerConfigForGuild(event.guild.id)

				if (config.miscellaneousConfig.enableQuirky)
					event.channel.sendMessage("First! <:lori_owo:417813932380520448>").queue()
			}
		}
	}
}