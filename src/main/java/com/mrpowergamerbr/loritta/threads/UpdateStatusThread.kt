package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import com.mrpowergamerbr.loritta.utils.loritta
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
	var lastUpdate: Long = System.currentTimeMillis()
	var currentIndex = 0 // Index atual
	var fanArtMinutes = -1
	var currentFanArt: LorittaConfig.LorittaFanArts = Loritta.config.fanArts[0]
	var currentDay = -1
	var revertedAvatar = false

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
		if (currentDay != Calendar.SUNDAY && !revertedAvatar) {
			revertedAvatar = true
			loritta.lorittaShards.shards[0].selfUser.manager.setAvatar(Icon.from(File("/home/servers/loritta/assets/avatar_fanarts/original.png"))).complete()
		}

		if (currentDay == -1) {
			currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
		}

		if (Loritta.config.fanArtExtravaganza && currentDay == Calendar.SUNDAY) {
			revertedAvatar = false
			if (currentIndex > Loritta.config.fanArts.size - 1) {
				currentIndex = 0
			}

			var minutes = Calendar.getInstance().get(Calendar.MINUTE) / 15
			val diff = System.currentTimeMillis() - lastUpdate

			if (diff >= 25000) {
				val fanArt = currentFanArt
				loritta.lorittaShards.setGame(EntityBuilder(loritta.lorittaShards.shards[0]).createGame("Fan Art by ${fanArt.artist} \uD83C\uDFA8 ~ Loritta Morenitta", "https://www.twitch.tv/mrpowergamerbr", Game.GameType.WATCHING))
				lastUpdate = System.currentTimeMillis()
			}

			if (fanArtMinutes != minutes) { // Diferente!
				fanArtMinutes = minutes
				if (currentIndex > Loritta.config.fanArts.size - 1) {
					currentIndex = 0
				}

				val fanArt = Loritta.config.fanArts[currentIndex]

				loritta.lorittaShards.shards[0].selfUser.manager.setAvatar(Icon.from(File("/home/servers/loritta/assets/avatar_fanarts/${fanArt.fileName}"))).complete()
				loritta.lorittaShards.setGame(EntityBuilder(loritta.lorittaShards.shards[0]).createGame("Fan Art by ${fanArt.artist} \uD83C\uDFA8 ~ Loritta Morenitta", "https://www.twitch.tv/mrpowergamerbr", Game.GameType.WATCHING))

				currentFanArt = fanArt
				currentIndex++
			}

			fanArtMinutes = minutes
		} else {
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
			str = str.replace("{guilds}", loritta.lorittaShards.getGuilds().size.toString())
			str = str.replace("{users}", loritta.lorittaShards.getUsers().size.toString())
			str = str.replace("{uptime}", sb.toString())

			val diff = System.currentTimeMillis() - lastUpdate

			if (diff >= 25000) {
				loritta.lorittaShards.setGame(EntityBuilder(loritta.lorittaShards.shards[0]).createGame(str, "https://www.twitch.tv/mrpowergamerbr", Game.GameType.valueOf(game.type)))
				currentIndex++
				lastUpdate = System.currentTimeMillis()

				if (currentIndex > Loritta.config.currentlyPlaying.size - 1) {
					currentIndex = 0
				}
			}
		}
	}
}