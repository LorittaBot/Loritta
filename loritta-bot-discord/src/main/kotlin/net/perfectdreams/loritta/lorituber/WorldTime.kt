package net.perfectdreams.loritta.lorituber

data class WorldTime(val currentTick: Long) {
    val hours: Int
        get() = ((currentTick / 60) % 24).toInt()
    val minutes: Int
        get() = ((currentTick % 60)).toInt()
}