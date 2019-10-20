package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.perfectdreams.loritta.utils.SimpleImageInfo
import net.perfectdreams.loritta.utils.readAllBytes
import org.apache.commons.io.IOUtils
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

object LorittaUtils {
	private val logger = KotlinLogging.logger {}

	fun canUploadFiles(context: CommandContext): Boolean {
		if (!context.isPrivateChannel && !context.guild.selfMember.hasPermission(context.event.textChannel!!, Permission.MESSAGE_ATTACH_FILES)) {
			context.message.channel.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.legacyLocale["IMAGE_UPLOAD_NO_PERM"].f() + " \uD83D\uDE22").queue()
			return false
		}
		return true
	}

	fun warnOwnerNoPermission(guild: Guild?, textChannel: TextChannel?, serverConf: MongoServerConfig) {
		if (textChannel == null || guild == null)
			return

		if (serverConf.warnOnMissingPermission) {
			for (member in guild.members) {
				if (!member.user.isBot && (member.hasPermission(Permission.ADMINISTRATOR) || member.hasPermission(Permission.MANAGE_PERMISSIONS))) {
					try {
						val locale = LorittaLauncher.loritta.getLegacyLocaleById(serverConf.localeId)
						member.user.openPrivateChannel().queue { channel -> channel.sendMessage(locale["LORITTA_HeyIDontHavePermission", textChannel.asMention, guild.name]).queue() }
					} catch (e: ErrorResponseException) {
						// Usuário tem as DMs desativadas
						if (e.errorResponse.code == 50007) {
							continue
						}
						e.printStackTrace()
					}

				}
			}
		}
	}

	@JvmOverloads
	fun downloadImage(url: String, connectTimeout: Int = 10, readTimeout: Int = 60, maxSize: Int = 16_777_216, overrideTimeoutsForSafeDomains: Boolean = false, maxWidth: Int = 2_500, maxHeight: Int = 2_500, bypassSafety: Boolean = false): BufferedImage? {
		try {
			val imageUrl = URL(url)
			val connection = if (bypassSafety) {
				imageUrl.openConnection()
			} else {
				imageUrl.openSafeConnection()
			} as HttpURLConnection

			connection.setRequestProperty("User-Agent",
					Constants.USER_AGENT)

			if (connection.getHeaderFieldInt("Content-Length", 0) > maxSize) {
				logger.warn { "Image $url exceeds the maximum allowed Content-Length! ${connection.getHeaderFieldInt("Content-Length", 0)} > $maxSize"}
				return null
			}

			if (connectTimeout != -1 && (!loritta.connectionManager.isTrusted(url) && overrideTimeoutsForSafeDomains)) {
				connection.connectTimeout = connectTimeout
			}

			if (readTimeout != -1 && (!loritta.connectionManager.isTrusted(url) && overrideTimeoutsForSafeDomains)) {
				connection.readTimeout = readTimeout
			}

			logger.debug { "Reading image $url; connectTimeout = $connectTimeout; readTimeout = $readTimeout; maxSize = $maxSize bytes; overrideTimeoutsForSafeDomains = $overrideTimeoutsForSafeDomains; maxWidth = $maxWidth; maxHeight = $maxHeight"}

			val imageBytes = connection.inputStream.readAllBytes(maxSize)

			val imageInfo = SimpleImageInfo(imageBytes)

			logger.debug { "Image $url was successfully downloaded! width = ${imageInfo.width}; height = ${imageInfo.height}; mimeType = ${imageInfo.mimeType}"}

			if (imageInfo.width > maxWidth || imageInfo.height > maxHeight) {
				logger.warn { "Image $url exceeds the maximum allowed width/height! ${imageInfo.width} > $maxWidth; ${imageInfo.height} > $maxHeight"}
				return null
			}

			return ImageIO.read(imageBytes.inputStream())
		} catch (e: Exception) {
		}

		return null
	}

	fun downloadFile(url: String, timeout: Int): InputStream? {
		try {
			val imageUrl = URL(url)
			val connection = imageUrl.openSafeConnection() as HttpURLConnection
			connection.setRequestProperty("User-Agent",
					Constants.USER_AGENT)

			if (timeout != -1) {
				connection.readTimeout = timeout
				connection.connectTimeout = timeout
			}

			return connection.inputStream
		} catch (e: Exception) {
		}

		return null
	}

	/**
	 * Verifica se um link é uma URL válida
	 *
	 * @param link
	 * @return se a URL é válida ou não
	 */
	fun isValidUrl(link: String): Boolean {
		try {
			URL(link)
			return true
		} catch (e: MalformedURLException) {
			return false
		}

	}

	fun getUUID(id: String): UUID {
		return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32))
	}

	@Throws(Exception::class)
	fun fetchRemoteFile(location: String): ByteArray? {
		val url = URL(location)
		val connection = url.openSafeConnection() as HttpURLConnection
		connection.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.3; WOW64; rv:37.0) Gecko/20100101 Firefox/37.0")
		var `is`: InputStream? = null
		var bytes: ByteArray? = null
		try {
			`is` = connection.inputStream
			bytes = IOUtils.toByteArray(`is`!!)
		} catch (e: IOException) {
			e.printStackTrace()
			//matches errors
		} finally {
			`is`?.close()
		}
		return bytes
	}

	fun toUnicode(ch: Int): String {
		return String.format("\\u%04x", ch)
	}

	fun evalMath(str: String): Double {
		return object : Any() {
			var pos = -1
			var ch: Int = 0

			fun nextChar() {
				ch = if (++pos < str.length) str[pos].toInt() else -1
			}

			fun eat(charToEat: Int): Boolean {
				while (ch == ' '.toInt()) nextChar()
				if (ch == charToEat) {
					nextChar()
					return true
				}
				return false
			}

			fun parse(): Double {
				nextChar()
				val x = parseExpression()
				if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
				return x
			}

			// Grammar:
			// expression = term | expression `+` term | expression `-` term
			// term = factor | term `*` factor | term `/` factor
			// factor = `+` factor | `-` factor | `(` expression `)`
			//        | number | functionName factor | factor `^` factor

			fun parseExpression(): Double {
				var x = parseTerm()
				while (true) {
					if (eat('+'.toInt()))
						x += parseTerm() // addition
					else if (eat('-'.toInt()))
						x -= parseTerm() // subtraction
					else
						return x
				}
			}

			fun parseTerm(): Double {
				var x = parseFactor()
				while (true) {
					if (eat('*'.toInt()))
						x *= parseFactor() // multiplication
					else if (eat('/'.toInt()))
						x /= parseFactor() // division
					else
						return x
				}
			}

			fun parseFactor(): Double {
				if (eat('+'.toInt())) return parseFactor() // unary plus
				if (eat('-'.toInt())) return -parseFactor() // unary minus

				var x: Double
				val startPos = this.pos
				if (eat('('.toInt())) { // parentheses
					x = parseExpression()
					eat(')'.toInt())
				} else if (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) { // numbers
					while (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) nextChar()
					x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
				} else if (ch >= 'a'.toInt() && ch <= 'z'.toInt()) { // functions
					while (ch >= 'a'.toInt() && ch <= 'z'.toInt()) nextChar()
					val func = str.substring(startPos, this.pos)
					x = parseFactor()
					if (func == "sqrt")
						x = Math.sqrt(x)
					else if (func == "sin")
						x = Math.sin(Math.toRadians(x))
					else if (func == "cos")
						x = Math.cos(Math.toRadians(x))
					else if (func == "tan")
						x = Math.tan(Math.toRadians(x))
					else
						throw RuntimeException("Unknown function: $func")
				} else {
					throw RuntimeException("Unexpected: " + ch.toChar())
				}

				if (eat('^'.toInt())) x = Math.pow(x, parseFactor()) // exponentiation
				if (eat('%'.toInt())) x %= parseFactor() // mod

				return x
			}
		}.parse()
	}
}
/**
 * Faz download de uma imagem e retorna ela como um BufferedImage
 * @param url
 * @return
 */
/**
 * Faz download de uma imagem e retorna ela como um BufferedImage
 * @param url
 * @param timeout
 * @return
 */