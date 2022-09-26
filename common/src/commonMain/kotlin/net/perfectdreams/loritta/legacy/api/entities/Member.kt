package net.perfectdreams.loritta.legacy.api.entities

import net.perfectdreams.loritta.legacy.api.OnlineStatus

interface Member : User {
	val onlineStatus: OnlineStatus
	// val voiceState: VoiceState?
}