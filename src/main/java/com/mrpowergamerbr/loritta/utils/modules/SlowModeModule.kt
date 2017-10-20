package com.mrpowergamerbr.loritta.utils.modules

import com.google.common.cache.CacheBuilder
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.util.concurrent.TimeUnit

object SlowModeModule {
	val slowModeDelay = CacheBuilder.newBuilder().expireAfterAccess(1L, TimeUnit.HOURS).build<String, Long>().asMap()

	fun checkForSlowMode(event: MessageReceivedEvent, config: ServerConfig): Boolean {
		if (!config.slowModeChannels.contains(event.textChannel.id))
			return false

		val delay = config.slowModeChannels[event.textChannel.id]!!
		val key = event.textChannel.id + "-" + event.author.name
		val lastMessageSent = slowModeDelay.getOrDefault(key, 0L)

		if (delay * 1000 > System.currentTimeMillis() - lastMessageSent) {
			event.message.delete().complete()
			return true
		}

		slowModeDelay[key] = System.currentTimeMillis()
		return false
	}
}