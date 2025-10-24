package net.perfectdreams.loritta.shimeji

enum class ActivityLevel(
    // These are in ticks
    val minElapsed: Int,
    val maxElapsed: Int
) {
    LOW(
        // 20s -> 45s
        400,
        900
    ),

    MEDIUM(
        // 0s -> 15s
        0, 300
    ),

    HIGH(
        // 0s -> 3s
        0, 60
    )
}