package net.perfectdreams.loritta.common.entities

interface User : Mentionable {
    val id: ULong
    val name: String
    val avatar: UserAvatar
}