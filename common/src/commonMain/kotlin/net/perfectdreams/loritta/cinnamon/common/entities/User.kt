package net.perfectdreams.loritta.cinnamon.common.entities

interface User : Mentionable {
    val id: ULong
    val name: String
    val avatar: UserAvatar
}