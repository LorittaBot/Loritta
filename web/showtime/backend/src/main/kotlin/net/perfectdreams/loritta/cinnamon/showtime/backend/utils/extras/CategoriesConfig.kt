package net.perfectdreams.loritta.cinnamon.showtime.backend.utils.extras

import kotlinx.serialization.Serializable

@Serializable
class CategoriesConfig(
    val categories: List<CategoryConfig>
)