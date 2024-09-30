package net.perfectdreams.loritta.lorituber.server.state

import net.perfectdreams.loritta.lorituber.server.state.items.LoriTuberGroceryItem

class GroceryStore(
    val id: String,
    val items: MutableList<LoriTuberGroceryItem>
) {
    var isDirty = false
}