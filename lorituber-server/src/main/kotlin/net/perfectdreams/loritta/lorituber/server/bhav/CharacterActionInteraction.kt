package net.perfectdreams.loritta.lorituber.server.bhav

import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter

data class CharacterActionInteraction<UseItemAttrType>(
    val character: LoriTuberCharacter,
    val usingItemTask: LoriTuberTask.UsingItem,
    val useItemAttributes: UseItemAttrType
)