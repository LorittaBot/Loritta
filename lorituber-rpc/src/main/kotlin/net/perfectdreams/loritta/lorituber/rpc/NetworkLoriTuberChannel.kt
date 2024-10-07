package net.perfectdreams.loritta.lorituber.rpc

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVideoContentCategory
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import java.util.*

@Serializable
data class NetworkLoriTuberChannel(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val pendingVideos: List<NetworkLoriTuberPendingVideo>,
    val subscribers: Int,
    val contentLevels: Map<LoriTuberVideoContentCategory, Int>,
    val channelRelationships: Map<LoriTuberVideoContentCategory, LoriTuberSuperViewerChannelRelationshipData>
) {
    @Serializable
    data class LoriTuberSuperViewerChannelRelationshipData(
        var relationshipScore: Int,
        var subscribers: Int
    )
}