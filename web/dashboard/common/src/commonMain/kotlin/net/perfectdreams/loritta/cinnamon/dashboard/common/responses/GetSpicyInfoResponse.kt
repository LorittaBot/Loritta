package net.perfectdreams.loritta.cinnamon.dashboard.common.responses

import kotlinx.serialization.Serializable

@Serializable
data class GetSpicyInfoResponse(
    // Easter egg
    val comment: String,
    val legacyDashboardUrl: String
) : LorittaResponse()