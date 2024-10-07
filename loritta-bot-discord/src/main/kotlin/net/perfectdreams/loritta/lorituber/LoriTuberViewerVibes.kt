package net.perfectdreams.loritta.lorituber

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.common.lorituber.LoriTuberVideoContentVibes

@Serializable
data class LoriTuberViewerVibes(
    val likedCategories: List<LoriTuberVideoContentCategory>,
    val vibes: Map<LoriTuberVideoContentVibes, Int>
)