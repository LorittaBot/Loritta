package net.perfectdreams.loritta.lorituber.server.state.items

import net.perfectdreams.loritta.lorituber.items.LoriTuberGroceryItemData

data class LoriTuberGroceryItem(val data: LoriTuberGroceryItemData) {
    val item = data.item.toItem()
    var inStock by data::inStock
}