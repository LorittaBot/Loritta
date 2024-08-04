package net.perfectdreams.loritta.morenitta.utils

import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.perfectdreams.loritta.cinnamon.discord.utils.images.InterpolationType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.getResizedInstance
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import net.perfectdreams.loritta.cinnamon.pudding.tables.BlacklistedGuilds
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.Profile
import org.jetbrains.exposed.sql.select
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

object LorittaUtils {
	private val logger = KotlinLogging.logger {}

	fun canUploadFiles(context: CommandContext): Boolean {
		if (!context.isPrivateChannel && !context.guild.selfMember.hasPermission(context.event.channel as GuildChannel, Permission.MESSAGE_ATTACH_FILES)) {
			context.message.channel.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["loritta.imageUploadNoPerm"].f() + " \uD83D\uDE22").queue()
			return false
		}
		return true
	}

	/**
	 * Downloads an image and returns it as a BufferedImage, additional checks are made and can be customized to avoid
	 * downloading unsafe/big images that crash the application.
	 *
	 * @param url                            the image URL
	 * @param connectTimeout                 the connection timeout
	 * @param readTimeout                    the read timeout
	 * @param maxSize                        the image's maximum size
	 * @param overrideTimeoutsForSafeDomains if the URL is a safe domain, ignore timeouts
	 * @param maxWidth                       the image's max width
	 * @param maxHeight                      the image's max height
	 * @param bypassSafety                   if the safety checks should be bypassed
	 *
	 * @return the image as a BufferedImage or null, if the image is considered unsafe
	 */
	@JvmOverloads
	fun downloadImage(loritta: LorittaBot, url: String, connectTimeout: Int = 10, readTimeout: Int = 60, maxSize: Int = 8_388_608 /* 8mib */, overrideTimeoutsForSafeDomains: Boolean = false, maxWidth: Int = 2_500, maxHeight: Int = 2_500, bypassSafety: Boolean = false): BufferedImage? {
		try {
			val imageBytes = downloadFile(loritta, url, connectTimeout, readTimeout, maxSize, overrideTimeoutsForSafeDomains) ?: return null

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

	data class CustomEmojiData(
		val isAnimated: Boolean,
		val name: String,
		val id: Long,
		val url: String,
	)

	fun retrieveEmoji(data: String): CustomEmojiData? {
		val emojiMentionPattern = "<(a)?:[^:]+:\\d+>".toRegex()

		return if (emojiMentionPattern.matches(data)) {
			CustomEmojiData(
				data.startsWith("<a:"),
				data.substringAfter(":").substringBeforeLast(":"),
				data.substringAfterLast(":").substringBefore(">").toLong(),
				"https://cdn.discordapp.com/emojis/${data.substringAfterLast(":").substringBefore(">").toLong()}.${if (data.startsWith("<a:")) "gif" else "png"}"
			)
		} else {
			null
		}
	}

	fun convertImage(byteArray: ByteArray, outputFormat: String, isSticker: Boolean = false): ByteArray? {
		return try {
			if (isSticker) {
				val image = ImageIO.read(ByteArrayInputStream(byteArray)).getResizedInstance(320, 320, InterpolationType.BILINEAR)
				val output = ByteArrayOutputStream()
				ImageIO.write(image, outputFormat, output)
				output.toByteArray()
			} else {
				val image = ImageIO.read(ByteArrayInputStream(byteArray))
				val output = ByteArrayOutputStream()
				ImageIO.write(image, outputFormat, output)
				output.toByteArray()
			}
		} catch (e: Exception) {
			e.printStackTrace()
			null
		}
	}

	/**
	 * Downloads an file and returns it as a ByteArray, additional checks are made and can be customized to avoid
	 * downloading unsafe/big files that crash the application.
	 *
	 * @param url                            the image URL
	 * @param connectTimeout                 the connection timeout
	 * @param readTimeout                    the read timeout
	 * @param maxSize                        the image's maximum size
	 * @param overrideTimeoutsForSafeDomains if the URL is a safe domain, ignore timeouts
	 * @param maxWidth                       the image's max width
	 * @param maxHeight                      the image's max height
	 * @param bypassSafety                   if the safety checks should be bypassed
	 *
	 * @return the image as a BufferedImage or null, if the image is considered unsafe
	 */
	@JvmOverloads
	fun downloadFile(loritta: LorittaBot, url: String, connectTimeout: Int = 10, readTimeout: Int = 60, maxSize: Int = 8_388_608 /* 8mib */, overrideTimeoutsForSafeDomains: Boolean = false, bypassSafety: Boolean = false): ByteArray? {
		try {
			val imageUrl = URL(url)
			val connection = if (bypassSafety) {
				imageUrl.openConnection()
			} else {
				imageUrl.openSafeConnection(loritta)
			} as HttpURLConnection

			connection.setRequestProperty(
				"User-Agent",
				Constants.USER_AGENT
			)

			val contentLength = connection.getHeaderFieldInt("Content-Length", 0)

			if (contentLength > maxSize) {
				logger.warn { "Image $url exceeds the maximum allowed Content-Length! ${connection.getHeaderFieldInt("Content-Length", 0)} > $maxSize"}
				return null
			}

			if (connectTimeout != -1 && (!loritta.connectionManager.isTrusted(url) && overrideTimeoutsForSafeDomains)) {
				connection.connectTimeout = connectTimeout
			}

			if (readTimeout != -1 && (!loritta.connectionManager.isTrusted(url) && overrideTimeoutsForSafeDomains)) {
				connection.readTimeout = readTimeout
			}

			logger.debug { "Reading image $url; connectTimeout = $connectTimeout; readTimeout = $readTimeout; maxSize = $maxSize bytes; overrideTimeoutsForSafeDomains = $overrideTimeoutsForSafeDomains; maxWidth" }

			val imageBytes = if (contentLength != 0) {
				// If the Content-Length is known (example: images on Discord's CDN do have Content-Length on the response header)
				// we can allocate the array with exactly the same size that the Content-Length provides, this way we avoid a lot of unnecessary Arrays.copyOf!
				// Of course, this could be abused to allocate a gigantic array that causes Loritta to crash, but if the Content-Length is present, Loritta checks the size
				// before trying to download it, so no worries :)
				connection.inputStream.readAllBytes(maxSize, contentLength)
			} else
				connection.inputStream.readAllBytes(maxSize)

			logger.debug { "File $url was successfully downloaded!"}

			return imageBytes
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

	fun toUnicode(ch: Int): String {
		return String.format("\\u%04x", ch)
	}

	/**
	 * Checks if the owner of the guild is banned and, if true, makes me quit the server
	 *
	 * This method checks if the [executorProfile] is the owner of the [guild] and, if it is, we don't load it from the database.
	 *
	 * @param executorProfile the profile of the user that invoked this action
	 * @param guild           the guild
	 * @return if the owner of the guild is banned
	 */
	suspend fun isGuildOwnerBanned(loritta: LorittaBot, executorProfile: Profile?, guild: Guild): Boolean {
		val ownerProfile = if (guild.ownerIdLong == executorProfile?.id?.value) executorProfile else loritta.getLorittaProfile(guild.ownerIdLong)
		return ownerProfile != null && isGuildOwnerBanned(loritta, guild, ownerProfile)
	}

	/**
	 * Checks if the owner of the guild is banned and, if true, makes me quit the server
	 *
	 * @param ownerProfile the profile of the guild's owner
	 * @param guild        the guild
	 * @return if the owner of the guild is banned
	 */
	suspend fun isGuildOwnerBanned(loritta: LorittaBot, guild: Guild, ownerProfile: Profile): Boolean {
		val bannedState = ownerProfile.getBannedState(loritta)

		if (bannedState != null && bannedState[BannedUsers.expiresAt] == null) { // Se o dono está banido e não é um ban temporário...
			if (!loritta.isOwner(ownerProfile.userId)) { // E ele não é o dono do bot!
				logger.info("Eu estou saindo do servidor ${guild.name} (${guild.id}) já que o dono ${ownerProfile.userId} está banido de me usar! ᕙ(⇀‸↼‶)ᕗ")
				guild.leave().queue() // Então eu irei sair daqui, me recuso a ficar em um servidor que o dono está banido! ᕙ(⇀‸↼‶)ᕗ
				return true
			}
		}
		return false
	}

	/**
	 * Checks if the guild is blacklisted and, if yes, makes me quit the server
	 *
	 * @param guild        the guild
	 * @return if the owner of the guild is banned
	 */
	suspend fun isGuildBanned(loritta: LorittaBot, guild: Guild): Boolean {
		val blacklisted = loritta.newSuspendedTransaction {
			BlacklistedGuilds.select {
				BlacklistedGuilds.id eq guild.idLong
			}.firstOrNull()
		}

		if (blacklisted != null) { // Se o servidor está banido...
			if (!loritta.isOwner(guild.ownerIdLong)) { // E ele não é o dono do bot!
				logger.info("Eu estou saindo do servidor ${guild.name} (${guild.id}) já que o servidor está banido de me usar! ᕙ(⇀‸↼‶)ᕗ *${blacklisted[BlacklistedGuilds.reason]}")
				guild.leave().queue() // Então eu irei sair daqui, me recuso a ficar em um servidor que o dono está banido! ᕙ(⇀‸↼‶)ᕗ
				return true
			}
		}
		return false
	}
}