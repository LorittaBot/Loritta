package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.LorittaLauncher
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.Profile
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.MiscUtil
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.BannedUsers
import net.perfectdreams.loritta.common.utils.Emotes
import org.apache.commons.lang3.ArrayUtils
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage

fun Image.toBufferedImage() : BufferedImage {
	return ImageUtils.toBufferedImage(this)
}

fun BufferedImage.makeRoundedCorners(cornerRadius: Int) : BufferedImage {
	return ImageUtils.makeRoundedCorner(this, cornerRadius)
}

fun Graphics.drawStringWrap(loritta: LorittaBot, text: String, x: Int, y: Int, maxX: Int = 9999, maxY: Int = 9999) {
	ImageUtils.drawTextWrap(loritta, text, x, y, maxX, maxY, this.fontMetrics, this)
}

fun Array<String>.remove(index: Int): Array<String> {
	return ArrayUtils.remove(this, index)
}

fun User.isLorittaSupervisor(lorittaShards: LorittaShards): Boolean {
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

val gson get() = LorittaBot.GSON

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
			= handleIfBanned(context.loritta, context.userHandle, profile, context.event.channel, context.locale)

	/**
	 * Checks if a user is banned and, if it is, a message is sent to the user via direct messages or, if their DMs are disabled, in the current channel.
	 *
	 * @return if the user is banned
	 */
	suspend fun handleIfBanned(context: DiscordCommandContext, profile: Profile)
			= handleIfBanned(context.loritta, context.user, profile, context.discordMessage.channel, context.locale)

	/**
	 * Checks if a user is banned and, if it is, a message is sent to the user via direct messages or, if their DMs are disabled, in the current channel.
	 * This also checks if the user is still banned, if not, remove it from the ignore list
	 *
	 * @param user           the user that will be checked if they are banned or not
	 * @param profile        the user's profile
	 * @param commandChannel where the banned message will be sent if the user's direct messages are disabled
	 * @param legacyLocale   the user's locale
	 * @return               if the user is banned
	 */
	private suspend fun handleIfBanned(loritta: LorittaBot, user: User, profile: Profile, commandChannel: MessageChannel, locale: BaseLocale): Boolean {
		val bannedState = profile.getBannedState(loritta)

		if (loritta.ignoreIds.contains(profile.userId)) { // Se o usuário está sendo ignorado...
			if (bannedState != null) { // E ele ainda está banido...
				logger.info { "${profile.id} tried to use me, but they are banned! >:)" }
				return true // Então flw galerinha
			} else {
				// Se não, vamos remover ele da lista do ignoreIds
				loritta.ignoreIds.remove(profile.userId)
				return false
			}
		}

		if (bannedState == null)
			return false

		loritta.ignoreIds.add(user.idLong)

		val message = locale.getList(
				"commands.youAreLorittaBanned",
				bannedState[BannedUsers.reason],
				bannedState[BannedUsers.expiresAt].let {
					if (it != null)
						DateUtils.formatMillis(it - System.currentTimeMillis(), locale)
					else
						locale["commands.command.mute.forever"]
				},
				loritta.config.loritta.website.url + "support",
				loritta.config.loritta.website.url + "guidelines",
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