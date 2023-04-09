package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.days

@Serializable
data class GamerSaferVerificationRole(
    val roleId: Long,
    val time: String
) {
    companion object {
        val allowedTimes = setOf(
            1.days,
            3.days,
            7.days
        )
    }

    init {
        require(time in allowedTimes.map { it.toIsoString() }) { "Unsupported Time" }
    }
}