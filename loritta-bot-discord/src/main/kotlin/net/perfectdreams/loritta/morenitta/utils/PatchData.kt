package net.perfectdreams.loritta.morenitta.utils

class PatchData {
	var willRestartAt: Long? = null
	var patchNotes: PatchNotes? = null

	data class PatchNotes(
			val receivedAt: Long,
			val expiresAt: Long,
			val blogPostId: String
	)
}