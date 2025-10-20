package net.perfectdreams.bliss

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.dashboard.EmbeddedToast

// We do it like this because we can't do a "catch all" with event listeners in JavaScript
// So we use the default "message" event and we deserialize based on that

@Serializable
sealed class SSEBliss

@Serializable
data class SSEBlissSwap(
    val content: String,
    val swap: String
) : SSEBliss()

@Serializable
data class SSEBlissShowToast(
    val toast: EmbeddedToast
) : SSEBliss()

@Serializable
data class SSECustomEvent(
    val event: String,
    val eventTarget: String
) : SSEBliss()