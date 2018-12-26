package net.perfectdreams.loritta.api.commands

import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.core.entities.User
import net.perfectdreams.loritta.api.entities.Message
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream

/**
 * Contexto do comando executado
 */
abstract class LorittaCommandContext(val locale: BaseLocale, val legacyLocale: LegacyBaseLocale) {
	abstract val message: Message

	abstract val guild: net.perfectdreams.loritta.api.entities.Guild?

	abstract fun getAsMention(addSpace: Boolean): String

	abstract suspend fun reply(message: String, prefix: String? = null, forceMention: Boolean = false): Message

	abstract suspend fun reply(vararg loriReplies: LoriReply): Message

	abstract suspend fun reply(mentionUserBeforeReplies: Boolean, vararg loriReplies: LoriReply): Message

	abstract suspend fun reply(image: BufferedImage, fileName: String, vararg loriReplies: LoriReply): Message

	abstract suspend fun sendMessage(message: String): Message

	abstract suspend fun sendFile(file: File, name: String, message: String): Message

	abstract suspend fun sendFile(image: BufferedImage, name: String, message: String): Message

	abstract suspend fun sendFile(inputStream: InputStream, name: String, message: String): Message

	/**
	 * Gets an image from the argument index via valid URLs at the specified index
	 *
	 * @param argument   the argument index on the rawArgs array
	 * @param search     how many messages will be retrieved from the past to get images (default: 25)
	 * @param avatarSize the size of retrieved user avatars from Discord (default: 2048)
	 * @return           the image object or null, if nothing was found
	 * @see              BufferedImage
	 */
	abstract suspend fun getImage(text: String, search: Int = 25, avatarSize: Int = 2048): BufferedImage?

	abstract suspend fun getUserAt(argument: Int): User?

	abstract suspend fun getUser(link: String?): net.perfectdreams.loritta.api.entities.User?

	abstract suspend fun getImageUrlAt(argument: Int, search: Int = 25, avatarSize: Int = 2048): String?

	abstract suspend fun getImageUrl(link: String?, search: Int, avatarSize: Int): String?

	abstract suspend fun getImageAt(argument: Int, search: Int, avatarSize: Int): BufferedImage?

	abstract suspend fun explain()
}