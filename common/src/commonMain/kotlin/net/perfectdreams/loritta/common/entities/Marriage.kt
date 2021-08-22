package net.perfectdreams.loritta.common.entities

import kotlinx.datetime.Instant

interface Marriage {
    val id: Long
    val user1: Long
    val user2: Long
    val marriedSince: Instant
}