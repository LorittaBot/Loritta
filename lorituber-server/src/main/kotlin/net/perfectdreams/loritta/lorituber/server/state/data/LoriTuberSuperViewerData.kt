package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVibes
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory

@Serializable
data class LoriTuberSuperViewerData(
    var preferredVideoContentCategories: List<LoriTuberVideoContentCategory>,
    var preferredVibes: LoriTuberVibes,
    var allocatedEngagement: Int
)