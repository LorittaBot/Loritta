package net.perfectdreams.loritta.watchdog.utils

class Bot {
	val clusters = mutableMapOf<Long, Cluster>()

	class Cluster {
		var startedAt = Long.MAX_VALUE
		var dead = false
		var areAllConnected = false
		var offlineForUpdates = false

		val isReady: Boolean
			get() = startedAt != Long.MAX_VALUE
	}
}