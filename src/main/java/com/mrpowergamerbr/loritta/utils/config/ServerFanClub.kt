package com.mrpowergamerbr.loritta.utils.config

data class ServerFanClub(
	val isSuper: Boolean, // Se o servidor é do SUPER fã clube da Loritta
	val id: String, // ID HTML do servidor
	val serverId: String, // ID do servidor no Discord
	var inviteUrl: String, // Invite do servidor
	val description: String // Descrição do servidor
)