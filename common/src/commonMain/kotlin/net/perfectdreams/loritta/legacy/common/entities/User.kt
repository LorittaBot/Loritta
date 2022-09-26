package net.perfectdreams.loritta.legacy.common.entities

interface User : Mentionable {
    val id: Long
    val name: String
    val avatar: UserAvatar
}