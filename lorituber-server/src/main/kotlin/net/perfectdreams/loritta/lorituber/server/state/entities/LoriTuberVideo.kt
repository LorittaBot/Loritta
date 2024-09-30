package net.perfectdreams.loritta.lorituber.server.state.entities

import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberVideoData

data class LoriTuberVideo(
    val id: Long,
    val data: LoriTuberVideoData
) : LoriTuberEntity()