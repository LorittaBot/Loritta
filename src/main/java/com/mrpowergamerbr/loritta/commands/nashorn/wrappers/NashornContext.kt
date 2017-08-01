package com.mrpowergamerbr.loritta.commands.nashorn.wrappers

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.nashorn.LorittaNashornException
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.utils.LorittaUtils

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Contexto do comando Nashorn executado, é simplesmente um wrapper "seguro" para comandos em JavaScript, para que
 * a Loritta possa controlar os comandos executados de uma maneira segura (para não abusarem da API do Discord)
 */
class NashornContext(
		private val context: CommandContext // Context original, jamais poderá ser usado pelo script!
) {
	var message: NashornMessage = NashornMessage(context.message)
	private var sentMessages = 0 // Quantas mensagens foram enviadas, usado para não levar rate limit
	private var lastMessageSent = 0L // Quando foi a última mensagem enviada
	var sender: NashornMember = NashornMember(context.handle)

	@NashornCommand.NashornDocs(arguments = "mensagem")
	fun reply(mensagem: String): NashornMessage {
		var diff = System.currentTimeMillis() - lastMessageSent

		if (sentMessages >= 3) {
			if (diff > 2000) {
				throw LorittaNashornException("Mais de 3 mensagens em menos de 2 segundos!")
			} else {
				lastMessageSent = 0L
				sentMessages = 0
			}
		}

		sentMessages++
		lastMessageSent = System.currentTimeMillis()
		return NashornMessage(context.sendMessage(context.getAsMention(true) + mensagem))
	}

	@NashornCommand.NashornDocs(arguments = "mensagem")
	fun sendMessage(mensagem: String): NashornMessage {
		var diff = System.currentTimeMillis() - lastMessageSent

		if (sentMessages >= 3) {
			if (diff > 2000) {
				throw LorittaNashornException("Mais de 3 mensagens em menos de 2 segundos!")
			} else {
				lastMessageSent = 0L
				sentMessages = 0
			}
		}

		sentMessages++
		lastMessageSent = System.currentTimeMillis()
		return NashornMessage(context.sendMessage(mensagem))
	}

	@Throws(NoSuchFieldException::class, IllegalAccessException::class, IOException::class)
	@NashornCommand.NashornDocs(arguments = "imagem, mensagem")
	@JvmOverloads fun sendImage(imagem: NashornImage, mensagem: String = " "): NashornMessage {
		var diff = System.currentTimeMillis() - lastMessageSent

		if (sentMessages >= 3) {
			if (diff > 2000) {
				throw LorittaNashornException("Mais de 3 mensagens em menos de 2 segundos!")
			} else {
				lastMessageSent = 0L
				sentMessages = 0
			}
		}

		sentMessages++
		lastMessageSent = System.currentTimeMillis()

		// Reflection, já que nós não podemos acessar o BufferedImage

		val field = imagem.javaClass.getDeclaredField("bufferedImage")
		field.isAccessible = true
		val bufferedImage = field.get(imagem) as BufferedImage

		val os = ByteArrayOutputStream()
		ImageIO.write(bufferedImage, "png", os)
		val `is` = ByteArrayInputStream(os.toByteArray())

		return NashornMessage(context.sendFile(`is`, "Loritta-NashornCommand.png", mensagem))
	}

	@NashornCommand.NashornDocs(arguments = "delimitador")
	@JvmOverloads fun joinArguments(delimitador: String = " "): String {
		return context.args.joinToString(delimitador).trim { it <= ' ' }
	}

	@NashornCommand.NashornDocs(arguments = "index, mensagem")
	fun isArgument(idx: Int, mensagem: String): Boolean {
		try {
			return mensagem == context.args[idx]
		} catch (e: IndexOutOfBoundsException) {
			return false
		}
	}

	@NashornCommand.NashornDocs(arguments = "index")
	fun getArgument(idx: Int): String? {
		try {
			return context.args[idx]
		} catch (e: IndexOutOfBoundsException) {
			return null
		}
	}

	@NashornCommand.NashornDocs(arguments = "index")
	fun getRawArgument(idx: Int): String? {
		try {
			return context.rawArgs[idx]
		} catch (e: IndexOutOfBoundsException) {
			return null
		}
	}

	@NashornCommand.NashornDocs(arguments = "index")
	fun getStrippedArgument(idx: Int): String? {
		try {
			return context.strippedArgs[idx]
		} catch (e: IndexOutOfBoundsException) {
			return null
		}
	}

	@NashornCommand.NashornDocs()
	fun getArguments(): Array<out String> {
		return context.args
	}

	@NashornCommand.NashornDocs()
	fun getRawArguments(): Array<out String> {
		return context.rawArgs
	}

	@NashornCommand.NashornDocs()
	fun getStrippedArguments(): Array<out String> {
		return context.strippedArgs
	}

	@NashornCommand.NashornDocs(arguments = "x, y")
	fun createImage(x: Int, y: Int): NashornImage {
		return NashornImage(x, y)
	}

	@NashornCommand.NashornDocs(arguments = "index")
	fun getImageFromContext(argumento: Int): NashornImage? {
		val bufferedImage = LorittaUtils.getImageFromContext(context, argumento)

		if (bufferedImage != null) {
			return NashornImage(bufferedImage)
		} else {
			return null
		}
	}

	@NashornCommand.NashornDocs()
	fun getGuild(): NashornGuild {
		return NashornGuild(context, context.message.guild)
	}
}