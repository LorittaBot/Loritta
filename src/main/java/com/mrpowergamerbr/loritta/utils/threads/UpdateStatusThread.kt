package com.mrpowergamerbr.loritta.utils.threads

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.core.entities.EntityBuilder
import net.dv8tion.jda.core.entities.Game
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

/**
 * Thread que atualiza o status da Loritta a cada 25s segundos
 */
class UpdateStatusThread : Thread("Update Status Thread") {
	var lastUpdate: Long = System.currentTimeMillis()
	var currentIndex = 0; // Index atual

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

		var str = Loritta.config.currentlyPlaying[currentIndex]
		str = str.replace("{guilds}", loritta.lorittaShards.getGuilds().size.toString())
		str = str.replace("{users}", loritta.lorittaShards.getUsers().size.toString())
		str = str.replace("{uptime}", sb.toString())

		val diff = System.currentTimeMillis() - lastUpdate

		if (diff >= 25000) {
			loritta.lorittaShards.setGame(EntityBuilder(loritta.lorittaShards.shards[0]).createGame(str, "https://www.twitch.tv/monstercat", Game.GameType.STREAMING))
			currentIndex++
			lastUpdate = System.currentTimeMillis()

			if (currentIndex > Loritta.config.currentlyPlaying.size - 1) {
				currentIndex = 0
			}
		}
	}
}