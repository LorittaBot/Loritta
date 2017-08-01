package com.mrpowergamerbr.loritta.commands.nashorn.wrappers

import net.dv8tion.jda.core.entities.Emote
import net.dv8tion.jda.core.entities.Message

/**
 * Wrapper de uma mensagem de um comando Nashorn executado, é simplesmente um wrapper "seguro" para comandos em JavaScript, para que
 * a Loritta possa controlar as mensagens enviadas de uma maneira segura (para não abusarem da API do Discord)
 */
class NashornMessage(private val message: Message) {
	fun editMessage(texto: String) {
		message.editMessage(texto).complete()
	}

	fun addReaction(texto: String) {
		val emotes = message.guild.getEmotesByName(texto, false)
		if (!emotes.isEmpty()) {
			message.addReaction(emotes[0])
		} else {
			message.addReaction(texto).complete()
		}
	}
}
