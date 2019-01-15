package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.Permission
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.util.*

class VolumeCommand : AbstractCommand("volume", category = CommandCategory.MUSIC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.get("VOLUME_DESCRIPTION")
	}

	override fun getExamples(): List<String> {
		return Arrays.asList("100", "66", "33")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.VOICE_MUTE_OTHERS)
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val premiumKey = loritta.getPremiumKey(context.config.premiumKey)

		if (premiumKey == null || 10 > premiumKey.paid) {
			context.reply(
					locale["PREMIUM_CantUseFeature"],
					"\uD83D\uDCB8"
			)
			return
		}

		val manager = loritta.audioManager.getGuildAudioPlayer(context.guild)
		if (context.args.isNotEmpty()) {
			try {
				val vol = Integer.valueOf(context.args[0])
				if (vol > 500) {
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["VOLUME_TOOHIGH"])
					return
				}
				if (0 > vol) {
					context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["VOLUME_TOOLOW"])
					return
				}

				manager.player.volume = context.args[0].toInt()

				// context.config.volume = manager.player.volume

				loritta save context.config

				if (manager.player.volume > vol) {
					context.reply(
							LoriReply(
									message = locale["VOLUME_LOWER"],
									prefix = "\uD83D\uDD08"
							)
					)
				} else {
					context.reply(
							LoriReply(
									message = locale["VOLUME_HIGHER"],
									prefix = "\uD83D\uDD0A"
							)
					)
				}
			} catch (e: Exception) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["VOLUME_EXCEPTION"])
			}
		} else {
			context.explain()
		}
	}
}