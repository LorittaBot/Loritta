package net.perfectdreams.loritta.lorituber.server.state.entities

import net.perfectdreams.loritta.lorituber.items.LoriTuberItem
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberCharacterData

data class LoriTuberCharacter(
    val id: Long,
    val data: LoriTuberCharacterData
) : LoriTuberEntity() {
    val motives = CharacterMotives(this)
    val inventory = CharacterInventory(this)

    fun setTask(newTask: LoriTuberTask?) {
        data.currentTask = newTask
        isDirty = true
    }

    class CharacterInventory(private val character: LoriTuberCharacter) {
        fun addItem(item: LoriTuberItem, quantity: Int) = addItem(item.id, quantity)

        fun addItem(itemId: LoriTuberItemId, quantity: Int) {
            val itemInInventory = character.data.items.firstOrNull { it.id == itemId }

            // We already have an item of the same type in our inventory!
            if (itemInInventory != null)
                itemInInventory.quantity++
            else
                character.data.items.add(LoriTuberItemStackData(itemId, quantity))

            character.isDirty = true
        }

        /**
         * Removes a single item from the inventory, decreasing their amount by 1
         *
         * @return if the item was successfully removed. if the user didn't have the item, it will return false.
         */
        fun removeSingleItem(itemId: LoriTuberItemId): Boolean {
            val itemInInventory = character.data.items.firstOrNull { it.id == itemId }

            // Player does not have item!
            if (itemInInventory == null)
                return false

            // We have it!
            itemInInventory.quantity--

            // The quantity is now zero, remove the item!
            if (itemInInventory.quantity == 0)
                character.data.items.remove(itemInInventory)

            character.isDirty = true

            return true
        }

        /**
         * Checks if the inventory contains all the items specified
         */
        fun containsItems(query: List<LoriTuberItemId>): Boolean {
            val missing = query.toMutableList()

            for (item in character.data.items) {
                if (item.id in missing) {
                    missing.remove(item.id)
                }

                if (missing.isEmpty())
                    return true
            }
            return false
        }
    }

    class CharacterMotives(private val character: LoriTuberCharacter) {
        val energy by character.data::energyNeed

        fun addHunger(value: Double) {
            character.data.hungerNeed = (character.data.hungerNeed + value).coerceIn(0.0, 100.0)
            character.isDirty = true
        }

        fun addEnergy(value: Double) {
            character.data.energyNeed = (character.data.energyNeed + value).coerceIn(0.0, 100.0)
            character.isDirty = true
        }
    }
}