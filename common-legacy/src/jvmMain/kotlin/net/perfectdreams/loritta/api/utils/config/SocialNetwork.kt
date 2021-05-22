package net.perfectdreams.loritta.utils.config

data class SocialNetwork(
    val type: Type

) {
    enum class Type {
        DISCORD
    }
}