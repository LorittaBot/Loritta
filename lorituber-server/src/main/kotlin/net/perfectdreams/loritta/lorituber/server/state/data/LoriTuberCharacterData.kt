package net.perfectdreams.loritta.lorituber.server.state.data

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import java.util.*

@Serializable
data class LoriTuberCharacterData(
    var ownerId: Long,
    @Serializable(UUIDSerializer::class)
    val mainLotId: UUID,
    @Serializable(UUIDSerializer::class)
    var currentLotId: UUID,
    var firstName: String,
    var lastName: String,
    var createdAtTick: Long,
    var ticksLived: Long,
    var energyNeed: Double,
    var hungerNeed: Double,
    var funNeed: Double,
    var hygieneNeed: Double,
    var bladderNeed: Double,
    var socialNeed: Double,
    var currentTask: LoriTuberTask?,
    val items: MutableList<LoriTuberItemStackData>,
    // TODO: Remove default
    var pendingPhoneCall: PendingPhoneCallData? = null,
    // TODO: Remove default
    var sonhos: Long = 0
)