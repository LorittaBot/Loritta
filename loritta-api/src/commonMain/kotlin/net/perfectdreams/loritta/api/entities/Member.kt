package net.perfectdreams.loritta.api.entities

import net.perfectdreams.loritta.api.OnlineStatus

interface Member : User {
	val onlineStatus: OnlineStatus
	// val voiceState: VoiceState?
}