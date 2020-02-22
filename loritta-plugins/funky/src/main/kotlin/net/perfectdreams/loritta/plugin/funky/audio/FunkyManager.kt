package net.perfectdreams.loritta.plugin.funky.audio

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
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
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

class FunkyManager(val loritta: Loritta, val audioManager: AudioManager) {
	companion object {
		private val logger = KotlinLogging.logger {}
		private val YOUTUBE_VIDEO_URL_REGEX = "(?:youtu.be\\/|v\\/|u\\/\\w\\/|embed\\/|watch\\?v=)([^#\\&\\?]*)".toPattern()
	}

	val lavalinkRestClient = LavalinkRestClient(loritta, audioManager)
	val musicQueue = ConcurrentHashMap<Long, GuildMusicManager>()

	fun connect(voiceChannel: VoiceChannel) = audioManager.connect(voiceChannel)

	fun getMusicManager(guild: Guild) = musicQueue[guild.idLong]
	fun getOrCreateMusicManager(guild: Guild, link: Link) = musicQueue.getOrPut(guild.idLong) {
		GuildMusicManager(this, link)
	}

	suspend fun resolveTrack(query: String): AudioTrack {
		var youTubeVideoId = getYouTubeVideoIdFromUrl(query)

		if (youTubeVideoId == null) {
			// Okay... então vamos tentar procurar no YouTube
			val tracks = lavalinkRestClient.searchTrackOnYouTube(query)
			// Vamos retornar o primeiro track! Mas antes vamos salvar todas as tracks carregadas na db...
			transaction(Databases.loritta) {
				for (track in tracks) {
					LavalinkTracks.insertIgnore {
						it[identifier] = track["info"]["identifier"].string
						it[trackData] = track["track"].string
						it[retrievedAt] = System.currentTimeMillis()
					}
				}
			}

			youTubeVideoId = tracks.first()["info"]["identifier"].string
		}

		val cachedAudioTrack = transaction(Databases.loritta) {
			LavalinkTracks.select { LavalinkTracks.identifier eq youTubeVideoId }.firstOrNull()
		}

		if (cachedAudioTrack != null) {
			logger.info { "Loaded $youTubeVideoId from the database, stored track ID: ${cachedAudioTrack[LavalinkTracks.identifier]}" }
			return LavalinkUtil.toAudioTrack(cachedAudioTrack[LavalinkTracks.trackData])
		}

		logger.info { "Loading $youTubeVideoId from Lavalink... Hang tight!" }
		val resolvedAudioTrackData = lavalinkRestClient.loadTrack(youTubeVideoId)
		transaction(Databases.loritta) {
			LavalinkTracks.insert {
				it[identifier] = youTubeVideoId
				it[trackData] = resolvedAudioTrackData
				it[retrievedAt] = System.currentTimeMillis()
			}
		}
		return LavalinkUtil.toAudioTrack(resolvedAudioTrackData)
	}

	private fun getYouTubeVideoIdFromUrl(url: String): String? {
		val regex = YOUTUBE_VIDEO_URL_REGEX.matcher(url)
		val find = regex.find()

		if (find)
			return regex.group(1)
		return null
	}
}