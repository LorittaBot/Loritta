package net.perfectdreams.loritta.plugin.funky.audio

import lavalink.client.io.Link

class GuildMusicManager(val funkyManager: FunkyManager, val link: Link) {
	val player = link.player
	val scheduler = TrackScheduler(funkyManager, link)

	init {
		player.addListener(scheduler)
	}
}