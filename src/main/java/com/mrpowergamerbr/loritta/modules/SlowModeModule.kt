package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import java.util.concurrent.TimeUnit

class SlowModeModule : MessageReceivedModule {
	companion object {
		val slowModeDelay = Caffeine.newBuilder().expireAfterAccess(1L, TimeUnit.HOURS).build<String, Long>().asMap()
	}

	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		if (!serverConfig.slowModeChannels.contains(event.channel.id))
			return false

		if (!event.guild!!.selfMember.hasPermission(event.textChannel, Permission.MESSAGE_MANAGE))
			return false

		if (lorittaUser.hasPermission(LorittaPermission.BYPASS_SLOW_MODE))
			return false

		return true
	}

	override fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: LorittaProfile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val delay = serverConfig.slowModeChannels[event.channel.id]!!
		val key = event.channel.id + "-" + event.author.name
		val lastMessageSent = slowModeDelay.getOrDefault(key, 0L)

		if (delay * 1000 > System.currentTimeMillis() - lastMessageSent) {
			event.message.delete().complete()
			return true
		}

		slowModeDelay[key] = System.currentTimeMillis()

		return false
	}
}