package com.mrpowergamerbr.loritta.nashorn.wrappers

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.nashorn.LorittaNashornException
import com.mrpowergamerbr.loritta.commands.nashorn.NashornCommand
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

/**
 * Contexto do comando Nashorn executado, é simplesmente um wrapper "seguro" para comandos em JavaScript, para que
 * a Loritta possa controlar os comandos executados de uma maneira segura (para não abusarem da API do Discord)
 */
class NashornContext(
		private val context: CommandContext // Context original, jamais poderá ser usado pelo script!
) {
	companion object {
		private val logger = KotlinLogging.logger {}

		fun securityViolation(guildId: String?) {
			try {
				throw IllegalArgumentException("Deu ruim!")
			} catch (e: Exception) {
				logger.error(e) { "Descobriram como pegar o meu token usando JavaScript na guild $guildId!!!" }
			}
			File("do_not_start").writeText("")
			System.exit(0)
		}
	}
	var message: NashornMessage = NashornMessage(context.message)
	private var sentMessages = 0 // Quantas mensagens foram enviadas, usado para não levar rate limit
	private var lastMessageSent = 0L // Quando foi a última mensagem enviada
	var sender: NashornMember = NashornLorittaUser(context.handle, context.config)

	val nashornGuild = NashornGuild(context.message.guild, context.config)

	@NashornCommand.NashornDocs("Envia uma mensagem no canal de texto atual, a mensagem será \"@Usuário mensagem\", caso a opção de mencionar usuários do servidor esteja desativada, a menção não aparecerá!",
	"mensagem",
"""
reply("Olá, eu me chamo Loritta!");
""")
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

		if (mensagem.contains(loritta.discordConfig.discord.clientToken, true))
			securityViolation(context.guild.id)

		sentMessages++
		lastMessageSent = System.currentTimeMillis()
		return runBlocking { NashornMessage(context.sendMessage(context.getAsMention(true) + mensagem)) }
	}

	@NashornCommand.NashornDocs("Envia uma mensagem no canal de texto atual.",
			"mensagem",
			"""
sendMessage("Olá, eu ainda me chamo Loritta!");
""")
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

		if (mensagem.contains(loritta.discordConfig.discord.clientToken, true))
			securityViolation(context.guild.id)

		sentMessages++
		lastMessageSent = System.currentTimeMillis()
		return runBlocking { NashornMessage(context.sendMessage(mensagem)) }
	}

	@Throws(NoSuchFieldException::class, IllegalAccessException::class, IOException::class)
	@NashornCommand.NashornDocs("Envia uma imagem no canal de texto atual.",
			"imagem, mensagem",
"""
var imagem = getImageFromContext(0); // Se você escrever "comando @Loritta", a imagem será o meu avatar!
imagem.write("fofa!", cor(128, 128, 128), 20, 20);
sendImage(imagem, "😄");
""")
	fun sendImage(imagem: NashornImage, mensagem: String = " "): NashornMessage {
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

		if (mensagem.contains(loritta.discordConfig.discord.clientToken, true))
			securityViolation(context.guild.id)

		runBlocking {
			val message = NashornMessage(context.sendFile(`is`, "Loritta-NashornCommand.png", mensagem))
		}
		`is`.close()
		`os`.close()
		return message
	}

	@NashornCommand.NashornDocs(arguments = "delimitador")
	fun joinArguments(delimitador: String = " "): String {
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

	@NashornCommand.NashornDocs("Retorna uma imagem dependendo do contexto atual.",
			"index",
	"""
var imagem = getImageFromContext(0); // Se você escrever "comando @Loritta", a imagem será o meu avatar!
imagem.write("fofa!", cor(128, 128, 128), 20, 20);
sendImage(imagem, "😄");
""")
	fun getImageFromContext(argumento: Int): NashornImage? {
		val bufferedImage = runBlocking { context.getImageAt(argumento) }

		if (bufferedImage != null) {
			return NashornImage(bufferedImage)
		} else {
			return null
		}
	}

	@NashornCommand.NashornDocs(
			"Retorna a guild (ou seja, o servidor) atual.",
			"",
"""
var guild = getGuild();
reply("Você está na guild " + guild.getName() + "! 😎");
"""
	)

	fun getGuild(): NashornGuild {
		return nashornGuild
	}
}