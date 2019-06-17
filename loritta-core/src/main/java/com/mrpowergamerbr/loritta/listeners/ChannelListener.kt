package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ChannelListener(val loritta: Loritta) : ListenerAdapter() {
	override fun onTextChannelCreate(event: TextChannelCreateEvent) {
		GlobalScope.launch(loritta.coroutineDispatcher) {
			val config = loritta.getServerConfigForGuild(event.guild.id)

			if (config.miscellaneousConfig.enableQuirky)
				event.channel.sendMessage("First! <:lori_owo:417813932380520448>").queue()
		}
	}
}