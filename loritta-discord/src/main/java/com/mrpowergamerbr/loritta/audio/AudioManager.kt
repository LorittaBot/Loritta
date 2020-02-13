package com.mrpowergamerbr.loritta.audio

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import lavalink.client.LavalinkUtil
import lavalink.client.io.Link
import lavalink.client.io.jda.JdaLavalink
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.VoiceChannel
import net.perfectdreams.loritta.tables.LavalinkTracks
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

class AudioManager(val loritta: Loritta) {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val lavalink = JdaLavalink(
			loritta.discordConfig.discord.clientId,
			loritta.discordConfig.discord.maxShards
	) { shardId ->
		lorittaShards.shardManager.getShardById(shardId)
	}
	val lavalinkRestClient = LavalinkRestClient(loritta, this)
	val musicQueue = ConcurrentHashMap<Long, GuildMusicManager>()

	init {
		loritta.discordConfig.lavalink.nodes.forEach {
			lavalink.addNode(it.name, URI("ws://${it.address}"), it.password)
		}
	}

	fun connect(voiceChannel: VoiceChannel): Link {
		val jdaLink = lavalink.getLink(voiceChannel.guild)
		jdaLink.connect(voiceChannel)
		return jdaLink
	}

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