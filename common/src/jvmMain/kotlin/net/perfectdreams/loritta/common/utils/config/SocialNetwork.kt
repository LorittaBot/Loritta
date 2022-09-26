package net.perfectdreams.loritta.morenitta.utils.config

data class SocialNetwork(
    val type: Type

) {
    enum class Type {
        DISCORD
    }
}