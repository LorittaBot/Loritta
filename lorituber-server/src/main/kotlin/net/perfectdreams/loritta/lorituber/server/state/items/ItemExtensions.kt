package net.perfectdreams.loritta.lorituber.server.state.items

import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId
import net.perfectdreams.loritta.lorituber.items.LoriTuberItems

fun LoriTuberItemId.toItem() = LoriTuberItems.getById(this)