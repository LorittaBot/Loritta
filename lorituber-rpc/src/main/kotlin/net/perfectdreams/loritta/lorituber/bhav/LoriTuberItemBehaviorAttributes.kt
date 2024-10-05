package net.perfectdreams.loritta.lorituber.bhav

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.PhoneCall

@Serializable
sealed class LoriTuberItemBehaviorAttributes {
    @Serializable
    data class Computer(
        var ticksLived: Long,
        var temperature: Long,
        var videoRenderTask: RenderingVideo?,
    ) : LoriTuberItemBehaviorAttributes() {
        @Serializable
        data class RenderingVideo(
            var channelId: Long,
            var pendingVideoId: Long
        )
    }

    @Serializable
    data class Phone(
        var pendingPhoneCall: PendingPhoneCallData?
    ) : LoriTuberItemBehaviorAttributes() {
        @Serializable
        data class PendingPhoneCallData(val expiresAt: Long, val phoneCall: PhoneCall)
    }

    @Serializable
    data class Toilet(
        var ticksUsedSinceLastUnclog: Long,
        var isClogged: Boolean,
        var unclogTicks: Long,
    ) : LoriTuberItemBehaviorAttributes()

    @Serializable
    data object Shower : LoriTuberItemBehaviorAttributes()
}