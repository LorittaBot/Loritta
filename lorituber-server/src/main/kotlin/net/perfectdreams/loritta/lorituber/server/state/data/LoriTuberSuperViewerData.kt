package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVibes

@Serializable
data class LoriTuberSuperViewerData(
    var preferredVibes: LoriTuberVibes
)