package com.mrpowergamerbr.loritta.nashorn.wrappers

import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.api.entities.User

/**
 * Wrapper de um user de um comando Nashorn executado, é simplesmente um wrapper "seguro" para comandos em JavaScript, para que
 * a Loritta possa controlar as mensagens enviadas de uma maneira segura (para não abusarem da API do Discord)
 */
open class NashornUser(internal val user: User) {
	@NashornCommand.NashornDocs("Retorna o nome do usuário.",
			"",
"""
reply("Seu verdadeiro nome no Discord: " + author().getName() + "#" + author().getDiscriminator() + " 👀");
""")
	fun getName(): String {
		return user.name
	}

	@NashornCommand.NashornDocs("Retorna o discriminador do usuário.",
			"",
			"""
reply("Seu verdadeiro nome no Discord: " + author().getName() + "#" + author().getDiscriminator() + " 👀");
""")
	fun getDiscriminator(): String {
		return user.discriminator
	}

	@NashornCommand.NashornDocs()
	fun getId(): String {
		return user.id
	}

	@NashornCommand.NashornDocs()
	fun getAvatarUrl(): String {
		return user.effectiveAvatarUrl
	}

	@NashornCommand.NashornDocs()
	fun getDefaultAvatarUrl(): String {
		return user.defaultAvatarUrl
	}

	@NashornCommand.NashornDocs()
	fun getAvatar(): NashornImage {
		return NashornImage(LorittaUtils.downloadImage(getAvatarUrl())!!)
	}

	@NashornCommand.NashornDocs()
	fun getAsMention(): String {
		return user.asMention
	}

	@NashornCommand.NashornDocs()
	fun isBot(): Boolean {
		return user.isBot
	}

	@NashornCommand.NashornDocs()
	fun isFake(): Boolean {
		return user.isFake
	}
}