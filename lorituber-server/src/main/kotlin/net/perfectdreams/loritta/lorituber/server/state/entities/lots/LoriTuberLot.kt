package net.perfectdreams.loritta.lorituber.server.state.entities.lots

import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberLotData
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberEntity
import java.util.*

abstract class LoriTuberLot(
    val id: UUID,
    val data: LoriTuberLotData
) : LoriTuberEntity() {
    /**
     * Teleports the [character] to the current lot
     */
    fun teleportCharacterToHere(character: LoriTuberCharacter) {
        // Change the character to this lot
        character.data.currentLotId = this.id

        // Set the current task to null
        character.setTask(null)

        onPostCharacterJoin(character)
    }

    /**
     * Runs after a character joins the current lot
     */
    open fun onPostCharacterJoin(character: LoriTuberCharacter) {}
}