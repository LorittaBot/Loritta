package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.ServerConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.channel.text.TextChannelCreateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.dao.servers.moduleconfigs.MiscellaneousConfig

class ChannelListener(val loritta: Loritta) : ListenerAdapter() {
	override fun onTextChannelCreate(event: TextChannelCreateEvent) {
		GlobalScope.launch(loritta.coroutineDispatcher) {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			val miscellaneousConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<MiscellaneousConfig?>(com.mrpowergamerbr.loritta.utils.loritta, ServerConfig::miscellaneousConfig)

			val enableQuirky = miscellaneousConfig?.enableQuirky ?: false

			if (enableQuirky)
				event.channel.sendMessage("First! <:lori_owo:417813932380520448>").queue()
		}
	}
}