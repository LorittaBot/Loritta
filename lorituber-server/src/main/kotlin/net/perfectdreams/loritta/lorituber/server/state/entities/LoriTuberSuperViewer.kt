package net.perfectdreams.loritta.lorituber.server.state.entities

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberSuperViewerData

@Serializable
data class LoriTuberSuperViewer(
    val id: Long,
    val data: LoriTuberSuperViewerData
)