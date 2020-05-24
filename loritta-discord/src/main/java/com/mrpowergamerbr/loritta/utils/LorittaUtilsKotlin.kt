package com.mrpowergamerbr.loritta.utils

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.stream.JsonReader
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Profile
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.utils.MiscUtil
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import org.apache.commons.lang3.ArrayUtils
import java.awt.Graphics
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.StringReader
import java.net.URLEncoder
import java.util.*

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
val jsonParser get() = Loritta.JSON_PARSER

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

	fun handleIfBanned(context: CommandContext, profile: Profile): Boolean {
		if (profile.isBanned) {
			LorittaLauncher.loritta.ignoreIds.add(context.userHandle.idLong)

			// Se um usuário está banido...
			context.userHandle
					.openPrivateChannel()
					.queue (
							{ it.sendMessage("\uD83D\uDE45 **|** " + context.getAsMention(true) + context.legacyLocale["USER_IS_LORITTABANNED", profile.bannedReason]).queue() },
							{ context.event.textChannel!!.sendMessage("\uD83D\uDE45 **|** " + context.getAsMention(true) + context.legacyLocale["USER_IS_LORITTABANNED", profile.bannedReason]).queue() }
					)
			return true
		}
		return false
	}


	fun handleIfBanned(context: LorittaCommandContext, profile: Profile): Boolean {
		if (context !is DiscordCommandContext)
			throw UnsupportedOperationException("I don't know how to handle a $context yet!")

		if (profile.isBanned) {
			LorittaLauncher.loritta.ignoreIds.add(context.userHandle.idLong)

			// Se um usuário está banido...
			context.userHandle
					.openPrivateChannel()
					.queue (
							{ it.sendMessage("\uD83D\uDE45 **|** " + context.getAsMention(true) + context.legacyLocale["USER_IS_LORITTABANNED", profile.bannedReason]).queue() },
							{ context.event.textChannel!!.sendMessage("\uD83D\uDE45 **|** " + context.getAsMention(true) + context.legacyLocale["USER_IS_LORITTABANNED", profile.bannedReason]).queue() }
					)
			return true
		}
		return false
	}

	fun handleIfBanned(context: net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext, profile: Profile): Boolean {
		if (profile.isBanned) {
			val legacyLocale = loritta.getLegacyLocaleById(context.locale.id)
			LorittaLauncher.loritta.ignoreIds.add(context.user.idLong)

			// Se um usuário está banido...
			context.user
					.openPrivateChannel()
					.queue (
							{ it.sendMessage("\uD83D\uDE45 **|** " + context.getUserMention(true) + legacyLocale["USER_IS_LORITTABANNED", profile.bannedReason]).queue() },
							{ context.discordMessage.channel.sendMessage("\uD83D\uDE45 **|** " + context.getUserMention(true) + legacyLocale["USER_IS_LORITTABANNED", profile.bannedReason]).queue() }
					)
			return true
		}
		return false
	}
}