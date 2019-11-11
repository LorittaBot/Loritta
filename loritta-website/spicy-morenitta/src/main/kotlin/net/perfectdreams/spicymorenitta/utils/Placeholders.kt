package net.perfectdreams.spicymorenitta.utils

object Placeholders {
	val DEFAULT_PLACEHOLDERS by lazy {
		mutableMapOf(
				"user" to "Mostra o nome do usuário que provocou a ação",
				"@user" to "Menciona o usuário que provocou a ação",
				"nickname" to " Mostra o nome do usuário no servidor (caso ele tenha mudado o apelido dele no servidor, irá aparecer o apelido dele, em vez do nome original)",
				"user-discriminator" to "Mostra o discriminator do usuário que provocou a ação",
				"user-id" to "Mostra o ID do usuário que provocou a ação",
				"user-avatar-url" to "Mostra a URL do avatar do usuário",
				"guild" to " Mostra o nome do servidor",
				"guild-size" to "Mostra a quantidade de membros no servidor"
		)
	}
}