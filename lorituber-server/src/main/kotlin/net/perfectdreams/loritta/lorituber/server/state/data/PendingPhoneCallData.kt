package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.PhoneCall

@Serializable
data class PendingPhoneCallData(val expiresAt: Long, val phoneCall: PhoneCall)