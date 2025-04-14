package net.perfectdreams.loritta.website.backend.utils.extras

import kotlinx.serialization.Serializable

@Serializable
class AuthorsConfig(
        val authors: List<AuthorConfig>
)