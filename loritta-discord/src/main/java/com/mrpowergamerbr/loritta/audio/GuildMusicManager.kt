package com.mrpowergamerbr.loritta.audio

import lavalink.client.io.Link

class GuildMusicManager(val audioManager: AudioManager, val link: Link) {
	val player = link.player
	val scheduler = TrackScheduler(audioManager, link)

	init {
		player.addListener(scheduler)
	}
}