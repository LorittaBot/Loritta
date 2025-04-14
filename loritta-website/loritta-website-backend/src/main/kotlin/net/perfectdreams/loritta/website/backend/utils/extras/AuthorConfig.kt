package net.perfectdreams.loritta.website.backend.utils.extras

import kotlinx.serialization.Serializable

@Serializable
data class AuthorConfig(
        val id: String,
        val name: String,
        val avatarUrl: String
)