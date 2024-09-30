package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable

@Serializable
data class LoriTuberSuperViewerChannelRelationshipData(
    var relationshipScore: Int,
    var subscribers: Int
)