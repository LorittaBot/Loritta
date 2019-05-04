package com.mrpowergamerbr.loritta.threads

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Icon
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
		var currentFanArt: LorittaConfig.LorittaAvatarFanArt? = null
	}

	var lastUpdate: Long = System.currentTimeMillis()
	var fanArtMinutes = -1
	var currentIndex = 0 // Index atual
	var currentDay = -1
	var revertedAvatar = false

	override fun run() {
		super.run()

		while (true) {
			try {
				updateStatus()
			} catch (e: Exception) {
				e.printStackTrace()
			}
			Thread.sleep(1000)
		}
	}

	fun updateStatus() {
		if (skipToIndex != -1) {
			currentIndex = skipToIndex
			skipToIndex = -1
		}
		val calendar = Calendar.getInstance()
		currentDay = calendar.get(Calendar.DAY_OF_WEEK)
		val firstInstance = loritta.lorittaShards.getShards().firstOrNull { it.status == JDA.Status.CONNECTED }

		if (Loritta.config.discord.fanArtExtravaganza.enabled) {
			if (currentDay != Loritta.config.discord.fanArtExtravaganza.dayOfTheWeek && !revertedAvatar) {
				if (firstInstance != null) {
					revertedAvatar = true
					firstInstance.selfUser.manager.setAvatar(Icon.from(File(Loritta.ASSETS, "avatar_fanarts/original.png"))).complete()
				}
			}
		}

		if (Loritta.config.discord.fanArtExtravaganza.enabled && currentDay == Loritta.config.discord.fanArtExtravaganza.dayOfTheWeek) {
			revertedAvatar = false
			if (currentIndex > Loritta.config.discord.fanArtExtravaganza.fanArts.size - 1) {
				currentIndex = 0
			}

			val minutes = calendar.get(Calendar.MINUTE) / 10
			val diff = System.currentTimeMillis() - lastUpdate
			val currentFanArt = Loritta.config.discord.fanArtExtravaganza.fanArts[0]

			if (diff >= 25000 && firstInstance != null) {
				val fanArt = currentFanArt
				val artist = lorittaShards.getUserById(fanArt.artistId)

				val displayName = if (fanArt.fancyName != null) {
					fanArt.fancyName
				} else if (artist != null) {
					artist.name + "#" + artist.discriminator
				} else {
					"¯\\_(ツ)_/¯"
				}
				loritta.lorittaShards.shardManager.setGame(Activity.of(Activity.ActivityType.WATCHING, "\uD83D\uDCF7 Fan Art by $displayName \uD83C\uDFA8 — \uD83D\uDC81 @Loritta fanarts", "https://www.twitch.tv/mrpowergamerbr"))
				lastUpdate = System.currentTimeMillis()
			}

			if (fanArtMinutes != minutes) { // Diferente!
				fanArtMinutes = minutes
				if (currentIndex > Loritta.config.discord.fanArtExtravaganza.fanArts.size - 1) {
					currentIndex = 0
				}

				val fanArt = Loritta.config.discord.fanArtExtravaganza.fanArts[currentIndex]

				if (firstInstance != null) {
					val artist = lorittaShards.getUserById(fanArt.artistId)

					val displayName = if (fanArt.fancyName != null) {
						fanArt.fancyName
					} else if (artist != null) {
						artist.name + "#" + artist.discriminator
					} else {
						"¯\\_(ツ)_/¯"
					}

					firstInstance.selfUser.manager.setAvatar(Icon.from(File(Loritta.ASSETS, "avatar_fanarts/${fanArt.fileName}"))).complete()
					loritta.lorittaShards.shardManager.setGame(Activity.of(Activity.ActivityType.WATCHING, "\uD83D\uDCF7 Fan Art by $displayName \uD83C\uDFA8 — \uD83D\uDC81 @Loritta fanarts", "https://www.twitch.tv/mrpowergamerbr"))

					UpdateStatusThread.currentFanArt = fanArt
					currentIndex++
				}
			}

			fanArtMinutes = minutes
		} else {
			val diff = System.currentTimeMillis() - lastUpdate

			if (diff >= 25000) {
				if (currentIndex > Loritta.config.discord.activities.size - 1) {
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

				val game = Loritta.config.discord.activities[currentIndex]

				var str = game.name
				str = str.replace("{guilds}", loritta.lorittaShards.getCachedGuildCount().toString())
				str = str.replace("{users}", loritta.lorittaShards.getCachedUserCount().toString())
				str = str.replace("{uptime}", sb.toString())

				loritta.lorittaShards.shardManager.setGame(Activity.of(Activity.ActivityType.valueOf(game.type), str, "https://www.twitch.tv/mrpowergamerbr"))
				currentIndex++
				lastUpdate = System.currentTimeMillis()

				if (currentIndex > Loritta.config.discord.activities.size - 1) {
					currentIndex = 0
				}
			}
		}
	}
}