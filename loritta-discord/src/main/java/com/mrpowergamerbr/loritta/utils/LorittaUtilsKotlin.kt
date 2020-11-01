package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.MiscUtil
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.tables.BannedUsers
import net.perfectdreams.loritta.utils.Emotes
import org.apache.commons.lang3.ArrayUtils
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage

fun Image.toBufferedImage() : BufferedImage {
	return ImageUtils.toBufferedImage(this)
}

fun BufferedImage.makeRoundedCorners(cornerRadius: Int) : BufferedImage {
	return ImageUtils.makeRoundedCorner(this, cornerRadius)
}

fun Graphics.drawStringWrap(text: String, x: Int, y: Int, maxX: Int = 9999, maxY: Int = 9999) {
	ImageUtils.drawTextWrap(text, x, y, maxX, maxY, this.fontMetrics, this)
}

fun Array<String>.remove(index: Int): Array<String> {
	return ArrayUtils.remove(this, index)
}

fun User.getLorittaProfile(loritta: Loritta = LorittaLauncher.loritta): Profile? {
	return loritta.getLorittaProfile(this.idLong)
}

val User.lorittaSupervisor: Boolean
	get() {
		val lorittaGuild = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

		if (lorittaGuild != null) {
			val role = lorittaGuild.getRoleById("351473717194522647")
			val member = lorittaGuild.getMember(this)

			if (member != null && role != null) {
				if (member.roles.contains(role))
					return true
			}
		}
		return false
	}

val User.support: Boolean
	get() {
		val lorittaGuild = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

		if (lorittaGuild != null) {
			val role = lorittaGuild.getRoleById("399301696892829706")
			val member = lorittaGuild.getMember(this)

			if (member != null && role != null) {
				if (member.roles.contains(role))
					return true
			}
		}
		return false
	}

/**
 * Retorna a instância atual da Loritta
 */
val loritta get() = LorittaLauncher.loritta

/**
 * Retorna a LorittaShards
 */
val lorittaShards get() = LorittaLauncher.loritta.lorittaShards

val gson get() = Loritta.GSON

fun String.isValidSnowflake(): Boolean {
	try {
		MiscUtil.parseSnowflake(this)
		return true
	} catch (e: NumberFormatException) {
		return false
	}
}

object LorittaUtilsKotlin {
	val logger = KotlinLogging.logger {}

	/**
	 * Checks if a user is banned and, if it is, a message is sent to the user via direct messages or, if their DMs are disabled, in the current channel.
	 *
	 * @return if the user is banned
	 */
	suspend fun handleIfBanned(context: CommandContext, profile: Profile)
			= handleIfBanned(context.userHandle, profile, context.event.channel, context.locale, context.legacyLocale)

	/**
	 * Checks if a user is banned and, if it is, a message is sent to the user via direct messages or, if their DMs are disabled, in the current channel.
	 *
	 * @return if the user is banned
	 */
	suspend fun handleIfBanned(context: LorittaCommandContext, profile: Profile): Boolean {
		if (context !is DiscordCommandContext)
			throw UnsupportedOperationException("I don't know how to handle a $context yet!")

		return handleIfBanned(context.userHandle, profile, context.discordMessage.channel, context.locale, context.legacyLocale)
	}

	/**
	 * Checks if a user is banned and, if it is, a message is sent to the user via direct messages or, if their DMs are disabled, in the current channel.
	 *
	 * @return if the user is banned
	 */
	suspend fun handleIfBanned(context: net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext, profile: Profile)
			= handleIfBanned(context.user, profile, context.discordMessage.channel, context.locale, loritta.getLegacyLocaleById(context.locale.id))

	/**
	 * Checks if a user is banned and, if it is, a message is sent to the user via direct messages or, if their DMs are disabled, in the current channel.
	 *
	 * @param user           the user that will be checked if they are banned or not
	 * @param profile        the user's profile
	 * @param commandChannel where the banned message will be sent if the user's direct messages are disabled
	 * @param legacyLocale   the user's locale
	 * @return               if the user is banned
	 */
	private suspend fun handleIfBanned(user: User, profile: Profile, commandChannel: MessageChannel, locale: BaseLocale, legacyLocale: LegacyBaseLocale): Boolean {
		val bannedState = profile.getBannedState() ?: return false

		LorittaLauncher.loritta.ignoreIds.add(user.idLong)

		val message = locale.getList(
				"commands.youAreLorittaBanned",
				bannedState[BannedUsers.reason],
				bannedState[BannedUsers.expiresAt].let {
					if (it != null)
						DateUtils.formatMillis(it - System.currentTimeMillis(), legacyLocale)
					else
						locale["commands.moderation.mute.forever"]
				},
				loritta.instanceConfig.loritta.website.url + "support",
				loritta.instanceConfig.loritta.website.url + "guidelines",
				Emotes.DEFAULT_DANCE,
				Emotes.LORI_DEMON
		).joinToString("\n")

		// Se um usuário está banido...
		user.openPrivateChannel()
				.queue (
						{ it.sendMessage(message).queue() },
						{ commandChannel.sendMessage(message).queue() }
				)
		return true
	}
}