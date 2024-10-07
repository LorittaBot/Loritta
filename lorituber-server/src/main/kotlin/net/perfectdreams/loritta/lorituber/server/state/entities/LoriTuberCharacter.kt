package net.perfectdreams.loritta.lorituber.server.state.entities

import net.perfectdreams.loritta.lorituber.items.LoriTuberItem
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberCharacterData
import net.perfectdreams.loritta.lorituber.server.state.data.PendingPhoneCallData
import java.util.*

data class LoriTuberCharacter(
    val id: UUID,
    val data: LoriTuberCharacterData
) : LoriTuberEntity() {
    val motives = CharacterMotives(this)
    val inventory = CharacterInventory(this)

    fun setTask(newTask: LoriTuberTask?) {
        data.currentTask = newTask
        isDirty = true
    }

    fun setPendingPhoneCall(pendingPhoneCallData: PendingPhoneCallData?) {
        data.pendingPhoneCall = pendingPhoneCallData
        isDirty = true
    }

    fun addSonhos(quantity: Long) {
        if (0 > quantity)
            error("Can't give negative sonhos! Quantity: $quantity")
        if (quantity == 0L)
            error("Can't give zero sonhos!")

        data.sonhos += quantity
        isDirty = true
    }

    fun removeSonhos(quantity: Long) {
        if (0 > quantity)
            error("Can't remove negative sonhos! Quantity: $quantity")
        if (quantity == 0L)
            error("Can't remove zero sonhos!")

        data.sonhos -= quantity
        isDirty = true
    }

    fun hasSonhos(quantity: Long): Boolean {
        return data.sonhos >= quantity
    }

    class CharacterInventory(private val character: LoriTuberCharacter) {
        fun addItem(item: LoriTuberItem, quantity: Int) = addItem(item.id, quantity)

        fun addItem(itemId: LoriTuberItemId, quantity: Int) {
            val itemInInventory = character.data.items.firstOrNull { it.id == itemId }

            // We already have an item of the same type in our inventory!
            if (itemInInventory != null)
                itemInInventory.quantity++
            else
                character.data.items.add(LoriTuberItemStackData(UUID.randomUUID(), itemId, quantity))

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
        companion object {
            private const val MAX_NEEDS = 100.5
        }

        val energy by character.data::energyNeed
        val hygiene by character.data::hygieneNeed
        val bladder by character.data::bladderNeed
        val funn by character.data::funNeed
        val mood: Double
            get() = (character.data.energyNeed + character.data.hungerNeed + character.data.funNeed + character.data.hygieneNeed + character.data.bladderNeed + character.data.socialNeed) / 6.0

        // To avoid users thinking "well why doesn't it go up to 100% when I finish *insert task here*?", we coerce in 100.5 as a "need overfill"

        fun addHunger(value: Double) {
            character.data.hungerNeed = (character.data.hungerNeed + value).coerceIn(0.0, MAX_NEEDS)
            character.isDirty = true
        }

        fun addHungerPerTicks(value: Double, ticks: Long) {
            val valueInATick = value / ticks
            addHunger(valueInATick)
        }

        fun addEnergy(value: Double) {
            character.data.energyNeed = (character.data.energyNeed + value).coerceIn(0.0, MAX_NEEDS)
            character.isDirty = true
        }

        fun addEnergyPerTicks(value: Double, ticks: Long) {
            val valueInATick = value / ticks
            addEnergy(valueInATick)
        }

        fun addHygiene(value: Double) {
            character.data.hygieneNeed = (character.data.hygieneNeed + value).coerceIn(0.0, MAX_NEEDS)
            character.isDirty = true
        }

        fun addHygienePerTicks(value: Double, ticks: Long) {
            val valueInATick = value / ticks
            addHygiene(valueInATick)
        }

        fun addBladder(value: Double) {
            character.data.bladderNeed = (character.data.bladderNeed + value).coerceIn(0.0, MAX_NEEDS)
            character.isDirty = true
        }

        fun addBladderPerTicks(value: Double, ticks: Long) {
            val valueInATick = value / ticks
            addBladder(valueInATick)
        }

        fun addFun(value: Double) {
            character.data.funNeed = (character.data.funNeed + value).coerceIn(0.0, MAX_NEEDS)
            character.isDirty = true
        }

        fun addFunPerTicks(value: Double, ticks: Long) {
            val valueInATick = value / ticks
            addFun(valueInATick)
        }

        fun isFunFull() = funn > 100.0

        /**
         * Checks if the current mood is above the minimum quantity needed to work on things such as videos
         */
        fun isMoodAboveRequiredForWork(): Boolean {
            return mood >= 50.0
        }
    }
}