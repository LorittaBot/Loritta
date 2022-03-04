package net.perfectdreams.showtime.backend.utils.extras

import kotlinx.serialization.Serializable

@Serializable
class CategoriesConfig(
    val categories: List<CategoryConfig>
)