package net.perfectdreams.loritta.serializable

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Marriage(
    val id: Long,
    val participants: List<UserId>,
    val marriedSince: Instant,
    val coupleName: String?,
    val coupleBadge: String?,
    val affinity: Int,
    val hugCount: Int,
    val kissCount: Int,
    val headPatCount: Int,
)