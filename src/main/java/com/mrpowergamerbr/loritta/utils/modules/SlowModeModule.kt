package com.mrpowergamerbr.loritta.utils.modules

import com.google.common.cache.CacheBuilder
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.PermissionOverride
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.util.concurrent.TimeUnit

object SlowModeModule {
	val slowModeDelay = CacheBuilder.newBuilder().expireAfterAccess(1L, TimeUnit.HOURS).build<String, Long>().asMap()

	fun checkForSlowMode(event: MessageReceivedEvent, lorittaUser: GuildLorittaUser, config: ServerConfig): Boolean {
		if (!config.slowModeChannels.contains(event.textChannel.id))
			return false

		if (!event.guild.selfMember.hasPermission(event.textChannel, Permission.MESSAGE_MANAGE))
			return false

		if (lorittaUser.hasPermission(LorittaPermission.BYPASS_SLOW_MODE))
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