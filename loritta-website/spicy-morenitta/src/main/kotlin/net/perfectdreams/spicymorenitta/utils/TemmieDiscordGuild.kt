package net.perfectdreams.spicymorenitta.utils

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class TemmieDiscordGuild constructor(
        val id: String,
        val name: String,
        @Optional val icon: String? = null,
        val owner: Boolean,
        val permissions: Int,
        val joined: Boolean
)