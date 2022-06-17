package net.perfectdreams.loritta.cinnamon.dashboard.common.responses

import kotlinx.serialization.Serializable

@Serializable
data class RedirectToUrlResponse(val url: String) : LorittaResponse(), PostSonhosBundlesResponse