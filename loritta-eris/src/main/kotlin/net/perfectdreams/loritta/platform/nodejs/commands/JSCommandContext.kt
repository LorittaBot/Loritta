package net.perfectdreams.loritta.platform.nodejs.commands

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.api.utils.image.Image
import net.perfectdreams.loritta.api.utils.image.JSImage
import nodecanvas.createCanvas
import nodecanvas.loadImage
import org.w3c.dom.url.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class JSCommandContext(loritta: LorittaBot, command: Command<CommandContext>, args: List<String>, message: Message, locale: BaseLocale) : CommandContext(loritta, command, args, message, locale) {
	override suspend fun user(argument: Int): User? {
		if (this.args.size > argument) { // Primeiro iremos verificar se existe uma imagem no argumento especificado
			val link = this.args[argument] // Ok, será que isto é uma URL?

			// Vamos verificar por menções, uma menção do Discord é + ou - assim: <@123170274651668480>
			for (user in this.message.mentionedUsers) {
				if (user.asMention == link.replace("!", "")) { // O replace é necessário já que usuários com nick tem ! no mention (?)
					// Diferente de null? Então vamos usar o avatar do usuário!
					return user
				}
			}
		}
		return null
	}

	override suspend fun imageUrl(argument: Int, searchPreviousMessages: Int): String? {
		if (this.args.size > argument) { // Primeiro iremos verificar se existe uma imagem no argumento especificado
			val link = this.args[argument] // Ok, será que isto é uma URL?

			if (isValidUrl(link))
				return link // Se é um link, vamos enviar para o usuário agora

			// Vamos verificar por usuários no argumento especificado
			val user = user(argument)
			if (user != null)
				return user.avatarUrl + "?size=256"
		}

		return null
	}

	override suspend fun image(argument: Int, searchPreviousMessages: Int, createTextAsImageIfNotFound: Boolean): Image? {
		var toBeDownloaded = imageUrl(argument)

		if (toBeDownloaded == null) {
			if (args.isNotEmpty() && createTextAsImageIfNotFound) {
				// return JVMImage(ImageUtils.createTextAsImage(256, 256, args.joinToString(" ")))
			}

			if (searchPreviousMessages != 0) {
				toBeDownloaded = imageUrl(argument, searchPreviousMessages)
			}
		}

		if (toBeDownloaded == null)
			return null

		// Vamos baixar a imagem!
		try {
			return loadImageAsImageImpl(toBeDownloaded)
		} catch (e: Exception) {
			return null
		}
	}

	override suspend fun explain() {
		message.channel.sendMessage("TODO: explain()")
	}

	suspend fun loadImageAsImageImpl(path: String): Image {
		return suspendCoroutine { cont ->
			loadImage(path).then({ it: dynamic ->
				val canvas = createCanvas(it.width, it.height)
				canvas.getContext("2d").drawImage(it, 0.0, 0.0)
				val jsImage = JSImage(canvas)
				cont.resume(jsImage)
			}, { cont.resumeWithException(it) }
			)
		}
	}

	fun isValidUrl(url: String): Boolean {
		try {
			URL(url)
			return true
		} catch (e: Throwable) {
			return false
		}
	}
}