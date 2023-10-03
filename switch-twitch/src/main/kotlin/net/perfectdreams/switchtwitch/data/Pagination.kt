package net.perfectdreams.switchtwitch.data

import kotlinx.serialization.Serializable

@Serializable
data class Pagination(
    val cursor: String? = null
)