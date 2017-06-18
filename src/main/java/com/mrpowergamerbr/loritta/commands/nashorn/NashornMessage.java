package com.mrpowergamerbr.loritta.commands.nashorn;

import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;

import java.util.List;

/**
 * Contexto de um comando Nashorn executado, é simplesmente um wrapper "seguro" para comandos em JavaScript, para que
 * a Loritta possa controlar as mensagens enviadas de uma maneira segura (para não abusarem da API do Discord)
 */
public class NashornMessage {
	private final Message message;

	public NashornMessage(Message message) {
		this.message = message;
	}

	public void editarMensagem(String texto) {
		message.editMessage(texto).complete();
	}

	public void adicionarReação(String texto) {
		List<Emote> emotes = message.getGuild().getEmotesByName(texto, false);
		if (!emotes.isEmpty()) {
			message.addReaction(emotes.get(0));
		} else {
			message.addReaction(texto).complete();
		}
	}
}
