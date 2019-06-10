package net.perfectdreams.loritta.website.utils.config

data class SocialNetwork(
    val type: Type

) {
    enum class Type {
        DISCORD
    }
}