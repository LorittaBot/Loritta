package net.perfectdreams.showtime.backend.utils.extras

import kotlinx.serialization.Serializable

@Serializable
class AuthorsConfig(
        val authors: List<AuthorConfig>
)