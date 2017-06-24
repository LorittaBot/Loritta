package com.mrpowergamerbr.loritta.utils.reminders

/**
 * Classe para guardar lembretes
 */
class Reminder {
	constructor()

	var guild: String? = null; // ID da Guild
	var textChannel: String? = null; // ID do canal de texto
	var remindMe: Long = 0L; // Tempo para ser lembrado
	var reason: String? = null; // Razão do lembrete

	constructor(guild: String, textChannel: String, remindMe: Long, reason: String) {
		this.guild = guild;
		this.textChannel = textChannel;
		this.remindMe = remindMe;
		this.reason = reason;
	}
}