package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import kotlin.time.Duration.Companion.days

@Serializable
data class GamerSaferVerificationUserAndRole(
    val userId: Long,
    val cachedUserInfo: CachedUserInfo?,
    val roleId: Long,
    val time: String
) {
    companion object {
        val allowedTimes = setOf(
            1.days,
            3.days,
            7.days,
            30.days,
            365.days
        )
    }

    init {
        require(time in allowedTimes.map { it.toIsoString() }) { "Unsupported Time" }
    }
}