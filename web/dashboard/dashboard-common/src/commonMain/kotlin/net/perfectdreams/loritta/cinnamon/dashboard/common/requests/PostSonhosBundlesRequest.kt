package net.perfectdreams.loritta.cinnamon.dashboard.common.requests

import kotlinx.serialization.Serializable

@Serializable
data class PostSonhosBundlesRequest(
    val id: Long
) : LorittaRequest()