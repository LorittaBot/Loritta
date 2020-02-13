package com.mrpowergamerbr.loritta.nashorn.wrappers

import com.mrpowergamerbr.loritta.commands.nashorn.LorittaNashornException
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.entities.TextChannel

/**
 * Wrapper de um text channel de um comando Nashorn executado, é simplesmente um wrapper "seguro" para comandos em JavaScript, para que
 * a Loritta possa controlar as mensagens enviadas de uma maneira segura (para não abusarem da API do Discord)
 */
class NashornTextChannel(private val textChannel: TextChannel) {
	var requesterLimiter = 0

	@NashornCommand.NashornDocs()
	fun getId(): String {
		return textChannel.id
	}

	@NashornCommand.NashornDocs()
	fun getTopic(): String {
		return textChannel.topic!!
	}

	@NashornCommand.NashornDocs()
	fun getAsMention(): String {
		return textChannel.asMention
	}

	@NashornCommand.NashornDocs()
	fun getName(): String {
		return textChannel.name
	}

	@NashornCommand.NashornDocs()
	fun isNSFW(): Boolean {
		return textChannel.isNSFW
	}

	@NashornCommand.NashornDocs()
	fun canTalk(): Boolean {
		return textChannel.canTalk()
	}

	@NashornCommand.NashornDocs()
	fun canTalk(member: NashornMember): Boolean {
		return textChannel.canTalk(member.member)
	}

	@NashornCommand.NashornDocs()
	fun sendMessage(mensagem: String): NashornMessage {
		if (mensagem.contains(loritta.discordConfig.discord.clientToken, true))
			NashornContext.securityViolation(null)

		if (requesterLimiter >= 3)
			throw LorittaNashornException("Mais de três mensagens em um único comando!")
		requesterLimiter++

		return NashornMessage(textChannel.sendMessage(mensagem).complete())
	}
}
