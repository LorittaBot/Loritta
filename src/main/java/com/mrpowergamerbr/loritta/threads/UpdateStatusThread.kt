package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.EntityBuilder
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Icon
import java.io.File
import java.lang.management.ManagementFactory
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Thread que atualiza o status da Loritta a cada 1s segundos
 */
class UpdateStatusThread : Thread("Update Status Thread") {
	companion object {
		var skipToIndex = -1 // owo
	}

	var lastUpdate: Long = System.currentTimeMillis()
	// var fanArtMinutes = -1
	var currentIndex = 0 // Index atual
	// var currentFanArt: LorittaConfig.LorittaAvatarFanArt = Loritta.config.fanArts[0]
	// var currentDay = -1
	// var revertedAvatar = false

	override fun run() {
		super.run()

		while (true) {
			try {
				updateStatus();
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(1000);
		}
	}

	fun updateStatus() {
		if (skipToIndex != -1) {
			currentIndex = skipToIndex
			skipToIndex = -1
		}

		val diff = System.currentTimeMillis() - lastUpdate

		if (diff >= 25000) {
			if (currentIndex > Loritta.config.currentlyPlaying.size - 1) {
				currentIndex = 0
			}
			var jvmUpTime = ManagementFactory.getRuntimeMXBean().uptime
			val days = TimeUnit.MILLISECONDS.toDays(jvmUpTime)
			jvmUpTime -= TimeUnit.DAYS.toMillis(days)
			val hours = TimeUnit.MILLISECONDS.toHours(jvmUpTime)
			jvmUpTime -= TimeUnit.HOURS.toMillis(hours)
			val minutes = TimeUnit.MILLISECONDS.toMinutes(jvmUpTime)
			jvmUpTime -= TimeUnit.MINUTES.toMillis(minutes)
			val seconds = TimeUnit.MILLISECONDS.toSeconds(jvmUpTime)

			val sb = StringBuilder(64)
			sb.append(days)
			sb.append("d ")
			sb.append(hours)
			sb.append("h ")
			sb.append(minutes)
			sb.append("m ")
			sb.append(seconds)
			sb.append("s")

			val game = Loritta.config.currentlyPlaying[currentIndex]

			var str = game.name
			str = str.replace("{guilds}", loritta.lorittaShards.getGuildCount().toString())
			str = str.replace("{users}", loritta.lorittaShards.getUserCount().toString())
			str = str.replace("{uptime}", sb.toString())

			val shard = lorittaShards.shards.firstOrNull() ?: return
			loritta.lorittaShards.setGame(EntityBuilder(shard).createGame(str, "https://www.twitch.tv/mrpowergamerbr", Game.GameType.valueOf(game.type)))
			currentIndex++
			lastUpdate = System.currentTimeMillis()

			if (currentIndex > Loritta.config.currentlyPlaying.size - 1) {
				currentIndex = 0
			}
		}
	}
}