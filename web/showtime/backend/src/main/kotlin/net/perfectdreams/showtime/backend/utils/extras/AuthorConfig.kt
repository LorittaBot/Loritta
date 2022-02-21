package net.perfectdreams.showtime.backend.utils.extras

import kotlinx.serialization.Serializable

@Serializable
data class AuthorConfig(
        val id: String,
        val name: String,
        val avatarUrl: String
)