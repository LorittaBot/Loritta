package net.perfectdreams.loritta.website.backend.utils.extras

import kotlinx.serialization.Serializable

@Serializable
data class CategoryConfig(
    val title: String,
    val entries: List<CategoryEntry>
) {
    @Serializable
    data class CategoryEntry(
        val files: Map<String, String>,
        val icon: String
    )
}