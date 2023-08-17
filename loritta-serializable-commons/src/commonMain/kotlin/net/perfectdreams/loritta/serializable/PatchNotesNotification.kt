package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
data class PatchNotesNotification(
    val path: String
)