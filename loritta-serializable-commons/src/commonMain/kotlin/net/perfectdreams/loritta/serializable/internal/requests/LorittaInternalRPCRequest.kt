package net.perfectdreams.loritta.serializable.internal.requests

import kotlinx.serialization.Serializable

@Serializable
sealed class LorittaInternalRPCRequest {
    @Serializable
    data object GetLorittaInfoRequest : LorittaInternalRPCRequest()
}