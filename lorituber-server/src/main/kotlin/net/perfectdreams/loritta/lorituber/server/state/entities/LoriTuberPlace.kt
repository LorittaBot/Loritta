package net.perfectdreams.loritta.lorituber.server.state.entities

import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberPlaceData

data class LoriTuberPlace(
    val id: Long,
    val data: LoriTuberPlaceData
) : LoriTuberEntity()