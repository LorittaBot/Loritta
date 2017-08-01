package com.mrpowergamerbr.loritta.commands.nashorn.wrappers

import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import net.dv8tion.jda.core.entities.User

/**
 * Wrapper de um user de um comando Nashorn executado, é simplesmente um wrapper "seguro" para comandos em JavaScript, para que
 * a Loritta possa controlar as mensagens enviadas de uma maneira segura (para não abusarem da API do Discord)
 */
open class NashornUser(internal val user: User) {
	@NashornCommand.NashornDocs()
	fun getName(): String {
		return user.name
	}

	@NashornCommand.NashornDocs()
	fun getDiscriminator(): String {
		return user.discriminator
	}

	@NashornCommand.NashornDocs()
	fun getAvatarUrl(): String {
		return user.effectiveAvatarUrl
	}

	@NashornCommand.NashornDocs()
	fun getAvatar(): NashornImage {
		return NashornImage(LorittaUtils.downloadImage(getAvatarUrl()))
	}
}