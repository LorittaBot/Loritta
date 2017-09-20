package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.hooks.ListenerAdapter

class UpdateTimeListener(internal val loritta: Loritta) : ListenerAdapter() {
	override fun onGenericEvent(event: Event) {
		super.onGenericEvent(event)
		lorittaShards.lastJdaEventTime[event.jda] = System.currentTimeMillis()
	}
}