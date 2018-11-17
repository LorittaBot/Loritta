package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import java.util.concurrent.TimeUnit

class AutomodModule : MessageReceivedModule {
	override fun matches(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		if (lorittaUser.hasPermission(LorittaPermission.BYPASS_AUTO_MOD))
			return false

		return true
	}

	override fun handle(event: LorittaMessageEvent, lorittaUser: LorittaUser, lorittaProfile: Profile, serverConfig: ServerConfig, locale: BaseLocale): Boolean {
		val message = event.message
		val textChannelConfig = serverConfig.getTextChannelConfig(message.channel.id)

		val automodConfig = textChannelConfig.automodConfig
		val automodCaps = automodConfig.automodCaps
		val automodSelfEmbed = automodConfig.automodSelfEmbed

		if (automodCaps.isEnabled) {
			val content = message.contentRaw.replace(" ", "")
			val capsThreshold = automodCaps.capsThreshold

			val length = content.length.toDouble()
			if (length >= automodCaps.lengthThreshold) {
				val caps = content.count { it.isUpperCase() }.toDouble()

				val percentage = (caps / length) * 100

				if (percentage >= capsThreshold) {
					if (automodCaps.deleteMessage && event.guild!!.selfMember.hasPermission(event.textChannel!!, Permission.MESSAGE_MANAGE))
						message.delete().queue()

					if (automodCaps.replyToUser && message.textChannel.canTalk()) {
						message.channel.sendMessage(
								MessageUtils.generateMessage(automodCaps.replyMessage,
										listOf(event.guild!!, event.member!!),
										event.guild
								)
						).queue {
							if (automodCaps.enableMessageTimeout && it.guild.selfMember.hasPermission(event.textChannel, Permission.MESSAGE_MANAGE)) {
								val delay = Math.min(automodCaps.messageTimeout * 1000, 60000)
								it.delete().queueAfter(delay.toLong(), TimeUnit.MILLISECONDS)
							}
						}
					}

					return true
				}
			}
		}

		/* if (automodSelfEmbed.isEnabled) {
			if (message.embeds.isNotEmpty()) {
				if (automodSelfEmbed.deleteMessage)
					message.delete().queue()

				if (automodSelfEmbed.replyToUser) {
					val message = message.channel.sendMessage(MessageUtils.generateMessage(automodSelfEmbed.replyMessage, listOf(event.guild!!, event.member!!), event.guild)).complete()

					if (automodSelfEmbed.enableMessageTimeout) {
						var delay = Math.min(automodSelfEmbed.messageTimeout * 1000, 60000)
						Thread.sleep(delay.toLong())
						message.delete().queue()
					}
				}
			}
		} */
		return false
	}
}