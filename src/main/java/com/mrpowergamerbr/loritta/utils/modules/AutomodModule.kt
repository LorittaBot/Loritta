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
			val content = message.contentRaw.replace(" ", "")
			val capsThreshold = automodCaps.capsThreshold

			var length = content.length.toDouble()
			var caps = content.count { it.isUpperCase() }.toDouble()

			var percentage = (caps / length) * 100

			if (percentage >= capsThreshold) {
				if (automodCaps.deleteMessage)
					message.delete().queue()

				if (automodCaps.replyToUser)
					message.channel.sendMessage(MessageUtils.generateMessage(automodCaps.replyMessage, event)).queue()

				return true
			}
		}
		return false
	}
}