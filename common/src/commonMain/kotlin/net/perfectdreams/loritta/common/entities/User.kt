package net.perfectdreams.loritta.common.entities

interface User : Mentionable {
    val id: Long
    val name: String
    val avatarUrl: String
}