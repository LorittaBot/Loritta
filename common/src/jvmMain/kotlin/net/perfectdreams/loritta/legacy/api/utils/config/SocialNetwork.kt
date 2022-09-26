package net.perfectdreams.loritta.legacy.utils.config

data class SocialNetwork(
    val type: Type

) {
    enum class Type {
        DISCORD
    }
}