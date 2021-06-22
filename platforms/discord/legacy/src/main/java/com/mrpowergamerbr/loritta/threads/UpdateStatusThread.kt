package com.mrpowergamerbr.loritta.threads

import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.config.GeneralConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Icon
import java.io.File
import java.util.*

/**
 * Thread que atualiza o status da Loritta a cada 1s segundos
 */
class UpdateStatusThread : Thread("Update Status Thread") {
	companion object {
		var skipToIndex = -1 // owo
		var currentFanArt: GeneralConfig.LorittaAvatarFanArt? = null
		private val logger = KotlinLogging.logger {}
	}

	var lastUpdate: Long = System.currentTimeMillis()
	var fanArtMinutes = -1
	var currentIndex = 0 // Index atual
	var currentDay = -1
	var revertedAvatar = false
	var currentAvatarPayloadHash: Int? = null

	override fun run() {
		super.run()

		while (true) {
			try {
				updateStatus()
			} catch (e: Exception) {
				logger.error(e) { "Something went wrong while trying to update the bot status!" }
			}
			sleep(1000)
		}
	}

	fun updateStatus() {
		if (skipToIndex != -1) {
			currentIndex = skipToIndex
			skipToIndex = -1
		}

		// Used to display the current Loritta cluster in the status
		val currentCluster = loritta.lorittaCluster

		val calendar = Calendar.getInstance()
		currentDay = calendar.get(Calendar.DAY_OF_WEEK)
		val firstInstance = loritta.lorittaShards.getShards().firstOrNull { it.status == JDA.Status.CONNECTED }

		// Check if Loritta needs to revert her avatar and status to the previous avatar/status
		// This is also used in any cluster, but only the master cluster can revert the avatar to its original state!!
		if (loritta.discordConfig.discord.fanArtExtravaganza.enabled) {
			if (currentDay != loritta.discordConfig.discord.fanArtExtravaganza.dayOfTheWeek && !revertedAvatar) {
				if (firstInstance != null) {
					revertedAvatar = true
					currentAvatarPayloadHash = null

					if (loritta.isMaster)
						firstInstance.selfUser.manager.setAvatar(Icon.from(File(Loritta.ASSETS, "avatar_fanarts/original.png"))).complete()

					loritta.lorittaShards.shardManager.setActivityProvider {
						Activity.of(
							Activity.ActivityType.valueOf(loritta.discordConfig.discord.activity.type),
							"${loritta.discordConfig.discord.activity.name} | Cluster ${currentCluster.id} [$it]"
						)
					}
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

			if (currentFanArt == null || fanArtMinutes != minutes) { // Diferente!
				fanArtMinutes = minutes
				if (currentIndex > loritta.discordConfig.discord.fanArtExtravaganza.fanArts.size - 1) {
					currentIndex = 0
				}

				val fanArt = loritta.discordConfig.discord.fanArtExtravaganza.fanArts[currentIndex]

				// Only update the avatar if we are in the first cluster
				if (firstInstance != null) {
					if (loritta.isMaster) // Apenas troque o avatar caso seja o cluster principal (ele que controla tudo!)
						firstInstance.selfUser.manager.setAvatar(Icon.from(File(Loritta.ASSETS, "avatar_fanarts/${fanArt.fileName}"))).complete()

					currentFanArt = fanArt
					currentIndex++
				}
			}

			if (diff >= 25_000 && firstInstance != null) {
				val currentFanArtInMasterCluster = runBlocking { lorittaShards.queryMasterLorittaCluster("/api/v1/loritta/current-fan-art-avatar").await() }.obj

				val artistId = currentFanArtInMasterCluster["artistId"].nullString

				// If the artist ID is null, then there isn't any avatar available!
				// We also check if the current payload hash is different than what we have stored
				// We do this to only update the status when we *really* need to
				if (artistId != null && currentAvatarPayloadHash != currentFanArtInMasterCluster.hashCode()) {
					val fancyName = currentFanArtInMasterCluster["fancyName"].nullString

					val artist = runBlocking { lorittaShards.retrieveUserInfoById(artistId.toLong()) }

					val displayName = fancyName ?: (artist?.name ?: "¯\\_(ツ)_/¯")

					// We use ".setActivityProvider" to show the shard in the status
					loritta.lorittaShards.shardManager.setActivityProvider {
						Activity.of(
							Activity.ActivityType.WATCHING,
							"\uD83D\uDCF7 Fan Art by $displayName \uD83C\uDFA8 | Cluster ${currentCluster.id} [$it]"
						)
					}

					currentAvatarPayloadHash = currentFanArtInMasterCluster.hashCode()
					lastUpdate = System.currentTimeMillis()
				}
			}

			fanArtMinutes = minutes
		}
	}
}
