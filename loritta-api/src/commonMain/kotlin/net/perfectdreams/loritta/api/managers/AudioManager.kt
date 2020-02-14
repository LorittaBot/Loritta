package net.perfectdreams.loritta.api.managers

import net.perfectdreams.loritta.api.entities.VoiceChannel

interface AudioManager {
	fun connect(channel: VoiceChannel)
	fun disconnect()
}