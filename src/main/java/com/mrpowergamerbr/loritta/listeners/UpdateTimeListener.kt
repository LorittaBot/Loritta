package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.LORITTA_SHARDS
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.hooks.ListenerAdapter

class UpdateTimeListener(internal val loritta: Loritta) : ListenerAdapter() {
	override fun onGenericEvent(event: Event) {
		LORITTA_SHARDS.lastJdaEventTime[event.jda] = System.currentTimeMillis()
	}
}