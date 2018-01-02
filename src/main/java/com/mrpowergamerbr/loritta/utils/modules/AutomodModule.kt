package com.mrpowergamerbr.loritta.utils.modules

import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.GuildLorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.MessageUtils
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

object AutomodModule {
	fun handleAutomod(event: MessageReceivedEvent, guild: Guild, lorittaProfile: GuildLorittaUser, serverConfig: ServerConfig): Boolean {
		val message = event.message
		val textChannelConfig = serverConfig.getTextChannelConfig(message.channel.id)

		if (lorittaProfile.hasPermission(LorittaPermission.BYPASS_AUTO_MOD))
			return false

		val automodConfig = textChannelConfig.automodConfig
		val automodCaps = automodConfig.automodCaps

		if (automodCaps.isEnabled) {
			val content = message.contentStripped.replace(" ", "")
			val capsThreshold = automodCaps.capsThreshold

			var length = content.length.toDouble()
			if (length >= automodCaps.lengthThreshold) {
				var caps = content.count { it.isUpperCase() }.toDouble()

				var percentage = (caps / length) * 100

				if (percentage >= capsThreshold) {
					if (automodCaps.deleteMessage)
						message.delete().queue()

					if (automodCaps.replyToUser) {
						val message = message.channel.sendMessage(MessageUtils.generateMessage(automodCaps.replyMessage, event)).complete()

						if (automodCaps.enableMessageTimeout) {
							var delay = Math.min(automodCaps.messageTimeout * 1000, 60000)
							Thread.sleep(delay.toLong())
							message.delete().queue()
						}
					}

					return true
				}
			}
		}
		return false
	}
}