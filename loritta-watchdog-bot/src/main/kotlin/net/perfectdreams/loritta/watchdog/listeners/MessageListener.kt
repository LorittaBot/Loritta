package net.perfectdreams.loritta.watchdog.listeners

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.watchdog.WatchdogBot
import java.util.concurrent.TimeUnit

class MessageListener(val m: WatchdogBot) : ListenerAdapter() {
	override fun onMessageReceived(event: MessageReceivedEvent) {
		val botCheck = m.config.checkBots.firstOrNull { it.botId == event.message.author.idLong }

		if (botCheck != null && botCheck.channelId == event.channel.idLong) {
			m.lastReply[event.author.idLong] = System.currentTimeMillis()
			event.message.delete().queueAfter(botCheck.timeout, TimeUnit.MILLISECONDS)
		}
	}
}