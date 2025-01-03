package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
sealed class SonhosTransferRequestMetadata {
    @Serializable
    data class APIInitiatedSonhosTransferRequestMetadata(
        val reason: String
    ) : SonhosTransferRequestMetadata()
}