package com.mrpowergamerbr.loritta.threads

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.nullString
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Icon
import java.io.File
import java.lang.management.ManagementFactory
import java.time.Instant
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Thread que atualiza o status da Loritta a cada 1s segundos
 */
class UpdateStatusThread : Thread("Update Status Thread") {
	companion object {
		var skipToIndex = -1 // owo
		var currentFanArt: GeneralConfig.LorittaAvatarFanArt? = null
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

		if (loritta.discordConfig.discord.fanArtExtravaganza.enabled && loritta.isMaster) { // Apenas reverta o avatar caso seja o cluster principal
			if (currentDay != loritta.discordConfig.discord.fanArtExtravaganza.dayOfTheWeek && !revertedAvatar) {
				if (firstInstance != null) {
					revertedAvatar = true
					firstInstance.selfUser.manager.setAvatar(Icon.from(File(Loritta.ASSETS, "avatar_fanarts/original.png"))).complete()
				}
			}
		}

		if (loritta.discordConfig.discord.fanArtExtravaganza.enabled && currentDay == loritta.discordConfig.discord.fanArtExtravaganza.dayOfTheWeek) {
			revertedAvatar = false
			if (currentIndex > loritta.discordConfig.discord.fanArtExtravaganza.fanArts.size - 1) {
				currentIndex = 0
			}

			val minutes = calendar.get(Calendar.MINUTE) / 10
			val diff = System.currentTimeMillis() - lastUpdate

			if (fanArtMinutes != minutes) { // Diferente!
				fanArtMinutes = minutes
				if (currentIndex > loritta.discordConfig.discord.fanArtExtravaganza.fanArts.size - 1) {
					currentIndex = 0
				}

				val fanArt = loritta.discordConfig.discord.fanArtExtravaganza.fanArts[currentIndex]

				if (firstInstance != null) {
					if (loritta.isMaster) // Apenas troque o avatar caso seja o cluster principal (ele que controla tudo!)
						firstInstance.selfUser.manager.setAvatar(Icon.from(File(Loritta.ASSETS, "avatar_fanarts/${fanArt.fileName}"))).complete()

					currentFanArt = fanArt
					currentIndex++
				}
			}

			if (diff >= 25_000 && firstInstance != null) {
				val currentFanArtInMasterCluster = runBlocking { lorittaShards.queryMasterLorittaCluster("/api/v1/loritta/current-fan-art-avatar").await() }

				val artistId = currentFanArtInMasterCluster["artistId"].nullString
				if (artistId != null) { // Se o artistId for nulo, então ele não está marcado!
					val fancyName = currentFanArtInMasterCluster["fancyName"].nullString

					val artist = runBlocking { lorittaShards.retrieveUserById(artistId) }

					val displayName = fancyName
							?: if (artist != null) {
								artist.name + "#" + artist.discriminator
							} else {
								"¯\\_(ツ)_/¯"
							}

					loritta.lorittaShards.shardManager.setActivity(Activity.of(Activity.ActivityType.WATCHING, "\uD83D\uDCF7 Fan Art by $displayName \uD83C\uDFA8 — \uD83D\uDC81 @Loritta fanarts", "https://www.twitch.tv/mrpowergamerbr"))
					lastUpdate = System.currentTimeMillis()
				}
			}

			fanArtMinutes = minutes
		} else {
			val diff = System.currentTimeMillis() - lastUpdate

			if (diff >= 25000) {
				if (currentIndex > loritta.discordConfig.discord.activities.size - 1) {
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

				val game = loritta.discordConfig.discord.activities[currentIndex]

				var str = game.name
				str = str.replace("{guilds}", runBlocking { lorittaShards.queryGuildCount() }.toString())
				str = str.replace("{uptime}", sb.toString())

				val willRestartAt = loritta.patchData.willRestartAt
				if (willRestartAt != null) {
					val instant = Instant.ofEpochMilli(willRestartAt).atZone(ZoneId.systemDefault())
					str = "\uD83D\uDEAB Inatividade Agendada: ${instant.hour.toString().padStart(2, '0')}:${instant.minute.toString().padStart(2, '0')}"
				}

				repeat(10) {
					val sponsor = loritta.sponsors.getOrNull(it)
					str = str.replace(
							"{sponsor-$it}",
							"\uD83D\uDCAB ${sponsor?.name}: ${sponsor?.link} | #ad https://loritta.website/sponsors"
					)
				}

				loritta.lorittaShards.shardManager.setActivity(Activity.of(Activity.ActivityType.valueOf(game.type), str, "https://www.twitch.tv/mrpowergamerbr"))
				currentIndex++
				lastUpdate = System.currentTimeMillis()

				val nextStatus = loritta.discordConfig.discord.activities.getOrNull(currentIndex)
				if (nextStatus != null) {
					repeat(10) {
						if (nextStatus.name == "{sponsor-$it}" && loritta.sponsors.getOrNull(it) == null)
							currentIndex++
					}
				}

				if (currentIndex > loritta.discordConfig.discord.activities.size - 1) {
					currentIndex = 0
				}
			}
		}
	}
}