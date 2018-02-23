package com.mrpowergamerbr.loritta.commands.vanilla.music

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.music.AudioTrackWrapper
import net.dv8tion.jda.core.Permission
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

class ShuffleCommand : AbstractCommand("shuffle", listOf("embaralhar", "aleatório"), category = CommandCategory.MUSIC, lorittaPermissions = listOf(LorittaPermission.DJ)) {
	override fun getDescription(locale: BaseLocale): String {
		return locale.get("SHUFFLE_Description")
	}

	override fun requiresMusicEnabled(): Boolean {
		return true
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val manager = LorittaLauncher.loritta.getGuildAudioPlayer(context.guild)

		val shuffled = manager.scheduler.queue.shuffled()
		manager.scheduler.queue.clear()

		for (audioTrackWrapper in shuffled) {
			manager.scheduler.queue.put(audioTrackWrapper)
		}

		context.reply(
				LoriReply(
						locale["SHUFFLE_QueueShuffled"],
						"\uD83D\uDD00"
				)
		)
	}
}