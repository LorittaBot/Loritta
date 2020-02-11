package com.mrpowergamerbr.loritta.audio

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import lavalink.client.io.Link
import lavalink.client.player.IPlayer
import lavalink.client.player.event.PlayerEventListenerAdapter
import mu.KotlinLogging
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(val audioManager: AudioManager, val link: Link) : PlayerEventListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	var currentlyPlaying: TrackRequest? = null
	val queue = LinkedBlockingQueue<TrackRequest>()
	var isLooping = false
	val player = link.player

	fun queue(track: TrackRequest) {
		queue.add(track)

		if (player.playingTrack == null)
			nextTrack()
	}

	fun nextTrack() {
		val request = queue.poll()

		// Trocar a próxima track caso possível
		if (request != null) {
			logger.info { "Playing ${request.track.info.title}" }
			play(request)
		} else {
			// Caso não exista nenhuma próxima música, vamos remover a queue e deletar o link
			audioManager.musicQueue.remove(link.guildIdLong)
			link.destroy()
		}
	}

	fun play(request: TrackRequest) {
		player.playTrack(request.track)
		currentlyPlaying = request
	}

	fun destroy() {
		// Caso não exista nenhuma próxima música, vamos remover a queue e deletar o link
		audioManager.musicQueue.remove(link.guildIdLong)
		link.destroy()
	}

	override fun onTrackEnd(player: IPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
		val wasCurrentlyPlaying = currentlyPlaying
		if (isLooping && wasCurrentlyPlaying != null) { // Se estamos em loop, vamos colocar readicionar a música atual na fila e tocar a próxima!
			queue.clear()
			queue.add(wasCurrentlyPlaying)
		}

		currentlyPlaying = null

		if (endReason.mayStartNext)
			nextTrack()
	}
}