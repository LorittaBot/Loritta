package net.perfectdreams.loritta.plugin.funky.audio

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.audio.AudioManager
import com.mrpowergamerbr.loritta.network.Databases
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import lavalink.client.LavalinkUtil
import lavalink.client.io.Link
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.VoiceChannel
import net.perfectdreams.loritta.plugin.funky.tables.LavalinkTracks
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

class FunkyManager(val loritta: Loritta, val audioManager: AudioManager) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val lavalinkRestClient = LavalinkRestClient(loritta, audioManager)
	val musicQueue = ConcurrentHashMap<Long, GuildMusicManager>()

	fun connect(voiceChannel: VoiceChannel) = audioManager.connect(voiceChannel)

	fun getMusicManager(guild: Guild) = musicQueue[guild.idLong]
	fun getOrCreateMusicManager(guild: Guild, link: Link) = musicQueue.getOrPut(guild.idLong) {
		GuildMusicManager(this, link)
	}

	suspend fun resolveTrack(audioId: String): AudioTrack {
		val cachedAudioTrack = transaction(Databases.loritta) {
			LavalinkTracks.select { LavalinkTracks.identifier eq audioId }.firstOrNull()
		}

		if (cachedAudioTrack != null) {
			logger.info { "Loaded $audioId from the database, stored track ID: ${cachedAudioTrack[LavalinkTracks.identifier]}" }
			return LavalinkUtil.toAudioTrack(cachedAudioTrack[LavalinkTracks.trackData])
		}

		logger.info { "Loading $audioId from Lavalink... Hang tight!" }
		val resolvedAudioTrackData = lavalinkRestClient.loadTrack(audioId)
		transaction(Databases.loritta) {
			LavalinkTracks.insert {
				it[identifier] = audioId
				it[trackData] = resolvedAudioTrackData
				it[retrievedAt] = System.currentTimeMillis()
			}
		}
		return LavalinkUtil.toAudioTrack(resolvedAudioTrackData)
	}
}